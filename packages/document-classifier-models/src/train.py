import os
import pandas as pd
import fasttext
from sklearn.metrics import accuracy_score, classification_report
from sklearn.model_selection import train_test_split


class TextClassifier:
  def __init__(self, num_labels=None, label_names=None):
    """Initialize the text classifier with fastText."""
    # Set label names and num_labels
    if label_names is None:
      raise ValueError("label_names must be provided")

    self.label_names = label_names
    self.num_labels = num_labels if num_labels is not None else len(label_names)
    self.model = None

    print(f"Classification categories: {list(self.label_names.values())}")

  def load_data(self, csv_path):
    """Load data from CSV file."""
    df = pd.read_csv(csv_path)
    return df

  def prepare_fasttext_data(self, df, output_file):
    """Convert DataFrame to fastText format and save to file."""
    # fastText format: __label__<label> <text>
    with open(output_file, 'w', encoding='utf-8') as f:
      for _, row in df.iterrows():
        label_idx = row['label']
        label_name = self.label_names[label_idx]
        text = row['text'].replace('\n', ' ').replace('\r', ' ')
        # Escape special characters for fastText
        text = text.replace('__label__', '')
        f.write(f"__label__{label_name} {text}\n")

  def prepare_dataset(self, df, output_dir="./results"):
    """Prepare the dataset for training and save in fastText format."""
    # Split the data (90% train, 10% validation)
    min_val_samples = max(4, int(len(df) * 0.1))
    test_size = min_val_samples / len(df)

    if len(df) < 10:
      train_df, val_df = train_test_split(
        df,
        test_size=test_size,
        random_state=42
      )
    else:
      train_df, val_df = train_test_split(
        df,
        test_size=test_size,
        random_state=42,
        stratify=df['label']
      )

    # Create output directory if it doesn't exist
    os.makedirs(output_dir, exist_ok=True)

    # Save in fastText format
    train_file = os.path.join(output_dir, "train.txt")
    val_file = os.path.join(output_dir, "val.txt")

    self.prepare_fasttext_data(train_df, train_file)
    self.prepare_fasttext_data(val_df, val_file)

    print(f"Training data saved to {train_file} ({len(train_df)} samples)")
    print(f"Validation data saved to {val_file} ({len(val_df)} samples)")

    return train_file, val_file, train_df, val_df

  def train(self, train_file, val_file, output_dir="./results"):
    """Train the fastText model with size-optimized parameters."""
    print("Starting fastText training with size optimizations...")
    print(f"- Training file: {train_file}")
    print(f"- Validation file: {val_file}")
    print("-" * 50)

    # Train fastText supervised model with reduced size parameters:
    # - dim: reduced from 100 to 50 (50% smaller vectors)
    # - wordNgrams: reduced from 2 to 1 (unigrams only, no bigrams)
    # - minCount: increased from 1 to 2 (filter rare words)
    # - bucket: reduced from default 2M to 50K (smaller hash table)
    model = fasttext.train_supervised(
      input=train_file,
      lr=0.1,
      epoch=25,
      wordNgrams=1,  # Unigrams only (was 2 for bigrams)
      dim=50,  # Reduced from 100 to 50
      minCount=2,  # Filter words appearing less than 2 times (was 1)
      bucket=50000,  # Reduced hash table size (default is 2000000)
      loss='hs',  # Hierarchical softmax for faster training
      verbose=2
    )

    # Save the original model
    model_path = os.path.join(output_dir, "best_model.bin")
    model.save_model(model_path)
    print(f"Model saved to {model_path}")

    # Get original model size
    original_size = os.path.getsize(model_path) / (1024 * 1024)  # MB
    print(f"Original model size: {original_size:.2f} MB")

    # Quantize the model to further reduce size (if model is large enough)
    # Quantization requires at least 256 rows in the matrix
    quantized_path = None
    try:
      print("\nAttempting to quantize model to reduce size...")
      model.quantize(input=train_file, retrain=True)
      quantized_path = os.path.join(output_dir, "best_model.ftz")
      model.save_model(quantized_path)

      # Get quantized model size
      quantized_size = os.path.getsize(quantized_path) / (1024 * 1024)  # MB
      print(f"Quantized model saved to {quantized_path}")
      print(f"Quantized model size: {quantized_size:.2f} MB")
      if original_size > 0:
        print(f"Size reduction: {(1 - quantized_size/original_size)*100:.1f}%")

      # Use the quantized model for inference (smaller and faster)
      self.model = model
    except ValueError as e:
      if "too small for quantization" in str(e) or "at least 256 rows" in str(e):
        print(f"Model too small for quantization (requires at least 256 rows): {e}")
        print("Using original model instead.")
        self.model = fasttext.load_model(model_path)
      else:
        raise
    except Exception as e:
      print(f"Quantization failed: {e}")
      print("Using original model instead.")
      self.model = fasttext.load_model(model_path)

    # Save labels.txt for reference
    labels_file = os.path.join(output_dir, "labels.txt")
    with open(labels_file, 'w', encoding='utf-8') as f:
      for idx in sorted(self.label_names.keys()):
        f.write(f"{self.label_names[idx]}\n")
    print(f"Labels saved to {labels_file}")

    # Evaluate on validation set
    print("\nEvaluating on validation set...")
    result = model.test(val_file)
    # fastText test() returns (number_of_samples, precision, recall)
    print(f"Validation samples: {result[0]}")
    print(f"Validation precision: {result[1]:.4f}")
    print(f"Validation recall: {result[2]:.4f}")

    return model

  def load_trained_model(self, model_path):
    """Load a trained fastText model for inference."""
    # Try to load quantized model first (.ftz), fall back to .bin
    if not model_path.endswith('.ftz') and not model_path.endswith('.bin'):
      # Try quantized version first
      quantized_path = model_path.replace('.bin', '.ftz')
      if os.path.exists(quantized_path):
        model_path = quantized_path
        print(f"Loading quantized model: {model_path}")
      elif os.path.exists(model_path + '.ftz'):
        model_path = model_path + '.ftz'
        print(f"Loading quantized model: {model_path}")

    self.model = fasttext.load_model(model_path)
    print(f"Model loaded from {model_path}")

  def predict(self, texts):
    """Predict labels for new texts."""
    if self.model is None:
      raise ValueError("Model not loaded. Call load_trained_model() first.")

    if isinstance(texts, str):
      texts = [texts]

    results = []
    for text in texts:
      # fastText expects clean text
      clean_text = text.replace('\n', ' ').replace('\r', ' ')
      clean_text = clean_text.replace('__label__', '')

      # Get predictions (returns top k=1 by default)
      labels, scores = self.model.predict(clean_text, k=1)

      # Extract label name (remove __label__ prefix)
      label_name = labels[0].replace('__label__', '')

      # Find label index
      label_idx = None
      for idx, name in self.label_names.items():
        if name == label_name:
          label_idx = idx
          break

      if label_idx is None:
        label_idx = 0
        label_name = "UNKNOWN"

      # fastText returns scores as probabilities
      confidence = float(scores[0])

      results.append({
        "text": text,
        "predicted_label": label_idx,
        "confidence": confidence,
        "category": label_name
      })

    return results

  def evaluate_model(self, test_texts, test_labels):
    """Evaluate the model on test data."""
    predictions = self.predict(test_texts)
    predicted_labels = [p["predicted_label"] for p in predictions]

    accuracy = accuracy_score(test_labels, predicted_labels)
    target_names = [self.label_names[i] for i in range(self.num_labels)]
    report = classification_report(test_labels, predicted_labels,
                                   target_names=target_names)

    print(f"Test Accuracy: {accuracy:.4f}")
    print("\nClassification Report:")
    print(report)

    return accuracy, report


def load_labels(labels_file):
  """Load label names from a text file, one label per line."""
  if not os.path.exists(labels_file):
    raise FileNotFoundError(f"Labels file not found: {labels_file}")

  with open(labels_file, 'r', encoding='utf-8') as f:
    labels = [line.strip() for line in f if line.strip()]

  # Create label mapping: index -> label name
  label_names = {i: label for i, label in enumerate(labels)}
  return label_names, len(labels)


def train_classifier(training_data_folder="training-data/event-categories", output_dir="./results"):
  """
  Train a text classifier from a training data folder.

  Args:
    training_data_folder: Path to folder containing data.csv and labels.txt
    output_dir: Directory where the trained model will be saved
  """
  print("Multi-Class Text Classification with fastText")
  print("=" * 50)

  # Load labels from labels.txt
  labels_file = os.path.join(training_data_folder, "labels.txt")
  data_file = os.path.join(training_data_folder, "data.csv")

  print(f"Loading labels from {labels_file}...")
  label_names, num_labels = load_labels(labels_file)
  print(f"Found {num_labels} labels: {list(label_names.values())}")

  # Initialize classifier with dynamic labels
  classifier = TextClassifier(num_labels=num_labels, label_names=label_names)

  # Load data
  print(f"Loading data from {data_file}...")
  df = classifier.load_data(data_file)
  print(f"Loaded {len(df)} samples")

  # Prepare datasets in fastText format
  print("Preparing datasets in fastText format...")
  train_file, val_file, train_df, val_df = classifier.prepare_dataset(df, output_dir=output_dir)

  # Train the model
  print("Training model...")
  classifier.train(train_file, val_file, output_dir=output_dir)

  # Example predictions with diverse categories
  print("\nMaking example predictions...")
  test_texts = [
    "The new MacBook Pro has incredible performance and battery life.",
    "The pizza at this Italian place was absolutely delicious!",
    "I watched an amazing sci-fi movie last night with great special effects.",
    "This online store has fast shipping and excellent customer service.",
    "My smartphone camera takes stunning photos in low light.",
    "The sushi restaurant has fresh fish and authentic Japanese atmosphere.",
    "The Netflix series was binge-worthy with an engaging storyline.",
    "Poor quality product, broke after just two days of use."
  ]

  predictions = classifier.predict(test_texts)

  print("\nPrediction Results:")
  print("-" * 70)
  for pred in predictions:
    print(f"Text: {pred['text']}")
    print(f"Category: {pred['category']}")
    print(f"Confidence: {pred['confidence']:.4f}")
    print("-" * 70)
