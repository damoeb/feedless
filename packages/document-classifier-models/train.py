import os
import pandas as pd
import torch
from datasets import Dataset
from sklearn.metrics import accuracy_score, classification_report
from sklearn.model_selection import train_test_split
from transformers import (
  AutoTokenizer,
  AutoModelForSequenceClassification,
  Trainer,
  TrainingArguments,
  DataCollatorWithPadding,
  EarlyStoppingCallback
)


class TextClassifier:
  def __init__(self, model_name="distilbert-base-uncased", num_labels=None, label_names=None):
    """Initialize the text classifier with a pre-trained model."""
    self.model_name = model_name

    # Set label names and num_labels
    if label_names is None:
      raise ValueError("label_names must be provided")

    self.label_names = label_names
    self.num_labels = num_labels if num_labels is not None else len(label_names)

    # Set up device with MPS fallback for Apple Silicon compatibility
    self.device = self._get_device()
    print(f"Using device: {self.device}")
    print(f"Classification categories: {list(self.label_names.values())}")

    self.tokenizer = AutoTokenizer.from_pretrained(model_name)
    self.model = AutoModelForSequenceClassification.from_pretrained(
      model_name,
      num_labels=num_labels
    )
    self.model.to(self.device)
    self.trainer = None

  def _get_device(self):
    """Get the best available device with MPS compatibility handling."""
    # Check if CPU is forced via environment variable
    if os.getenv("PYTORCH_ENABLE_MPS_FALLBACK") == "1" or os.getenv("FORCE_CPU") == "1":
      print("CPU usage forced via environment variable")
      return torch.device("cpu")

    if torch.cuda.is_available():
      return torch.device("cuda")
    elif torch.backends.mps.is_available():
      # Check if MPS is problematic by testing a simple operation
      try:
        # Test MPS with a simple embedding operation
        test_tensor = torch.tensor([1, 2, 3], device="mps")
        test_embedding = torch.nn.Embedding(10, 5).to("mps")
        _ = test_embedding(test_tensor)
        return torch.device("mps")
      except Exception as e:
        print(f"MPS device has compatibility issues: {e}")
        print("Falling back to CPU for stability...")
        print("You can also set PYTORCH_ENABLE_MPS_FALLBACK=1 to force CPU usage")
        return torch.device("cpu")
    else:
      return torch.device("cpu")

  def load_data(self, csv_path):
    """Load data from CSV file."""
    df = pd.read_csv(csv_path)
    return df

  def tokenize_function(self, examples):
    """Tokenize the text data."""
    return self.tokenizer(
      examples["text"],
      truncation=True,
      padding=True,
      max_length=512
    )

  def prepare_dataset(self, df):
    """Prepare the dataset for training."""
    # Split the data (90% train, 10% validation)
    # For small datasets, ensure we have at least one sample per class in validation
    min_val_samples = max(4, int(len(df) * 0.1))  # At least 4 samples for 4 classes
    test_size = min_val_samples / len(df)

    if len(df) < 10:
      # For very small datasets, use a simple split without stratification
      train_texts, val_texts, train_labels, val_labels = train_test_split(
        df['text'].tolist(),
        df['label'].tolist(),
        test_size=test_size,
        random_state=42
      )
    else:
      train_texts, val_texts, train_labels, val_labels = train_test_split(
        df['text'].tolist(),
        df['label'].tolist(),
        test_size=test_size,
        random_state=42,
        stratify=df['label'].tolist()
      )

    # Create datasets
    train_dataset = Dataset.from_dict({
      'text': train_texts,
      'labels': train_labels
    })

    val_dataset = Dataset.from_dict({
      'text': val_texts,
      'labels': val_labels
    })

    # Tokenize datasets
    train_dataset = train_dataset.map(self.tokenize_function, batched=True)
    val_dataset = val_dataset.map(self.tokenize_function, batched=True)

    return train_dataset, val_dataset

  def compute_metrics(self, eval_pred):
    """Compute metrics for evaluation."""
    predictions, labels = eval_pred
    predictions = predictions.argmax(axis=-1)

    accuracy = accuracy_score(labels, predictions)
    return {"accuracy": accuracy}

  def train(self, train_dataset, val_dataset, output_dir="./results"):
    """Train the model."""
    # Data collator
    data_collator = DataCollatorWithPadding(tokenizer=self.tokenizer)

    # Training arguments with early stopping configuration
    training_args = TrainingArguments(
      output_dir=output_dir,
      learning_rate=2e-5,
      per_device_train_batch_size=16,
      per_device_eval_batch_size=16,
      num_train_epochs=10,  # Increased epochs, early stopping will prevent overfitting
      weight_decay=0.01,
      eval_strategy="epoch",
      save_strategy="epoch",
      logging_dir=f"{output_dir}/logs",
      logging_steps=10,
      load_best_model_at_end=True,
      metric_for_best_model="eval_loss",  # Monitor validation loss for early stopping
      greater_is_better=False,  # Lower loss is better
      save_total_limit=3,  # Keep only the best 3 models to save space
    )

    # Initialize trainer with early stopping callback
    early_stopping_callback = EarlyStoppingCallback(
      early_stopping_patience=3,  # Stop if no improvement for 3 epochs
      early_stopping_threshold=0.001  # Minimum improvement threshold
    )

    self.trainer = Trainer(
      model=self.model,
      args=training_args,
      train_dataset=train_dataset,
      eval_dataset=val_dataset,
      tokenizer=self.tokenizer,
      data_collator=data_collator,
      compute_metrics=self.compute_metrics,
      callbacks=[early_stopping_callback],
    )

    # Train the model
    print("Starting training with early stopping...")
    print(f"- Training on {len(train_dataset)} samples (90%)")
    print(f"- Validating on {len(val_dataset)} samples (10%)")
    print(f"- Early stopping patience: 3 epochs")
    print(f"- Monitoring validation loss for overfitting")
    print("-" * 50)

    self.trainer.train()

    # Update model config with meaningful label names before saving
    id2label = {str(k): v for k, v in self.label_names.items()}
    label2id = {v: k for k, v in self.label_names.items()}
    self.model.config.id2label = id2label
    self.model.config.label2id = label2id

    # Save the model
    self.trainer.save_model(f"{output_dir}/best_model")
    print(f"Model saved to {output_dir}/best_model")
    print(f"Labels configured: {list(self.label_names.values())}")

    return self.trainer

  def load_trained_model(self, model_path):
    """Load a trained model for inference."""
    self.model = AutoModelForSequenceClassification.from_pretrained(model_path)
    self.model.to(self.device)
    self.tokenizer = AutoTokenizer.from_pretrained(model_path)
    print(f"Model loaded from {model_path}")

  def predict(self, texts):
    """Predict labels for new texts."""
    if isinstance(texts, str):
      texts = [texts]

    # Tokenize the input texts
    inputs = self.tokenizer(
      texts,
      truncation=True,
      padding=True,
      max_length=512,
      return_tensors="pt"
    )

    # Move inputs to the same device as the model
    inputs = {k: v.to(self.device) for k, v in inputs.items()}

    # Make predictions
    self.model.eval()
    with torch.no_grad():
      outputs = self.model(**inputs)
      predictions = torch.nn.functional.softmax(outputs.logits, dim=-1)
      predicted_labels = torch.argmax(predictions, dim=-1)

    # Convert to readable format
    results = []
    for i, text in enumerate(texts):
      label = predicted_labels[i].item()
      confidence = predictions[i][label].item()
      category_name = self.label_names.get(label, f"Unknown_{label}")
      results.append({
        "text": text,
        "predicted_label": label,
        "confidence": confidence,
        "category": category_name
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
  print("Multi-Class Text Classification with DistilBERT")
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

  # Prepare datasets
  print("Preparing datasets...")
  train_dataset, val_dataset = classifier.prepare_dataset(df)

  # Train the model
  print("Training model...")
  classifier.train(train_dataset, val_dataset, output_dir=output_dir)

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
