import sys
import os
from train import train_classifier
from convert_to_onnx import convert_to_onnx

if __name__ == "__main__":
  # Parse command-line arguments
  # Usage: python main.py [training_data_folder] [output_dir] [onnx_output_dir]

  if len(sys.argv) > 1:
    training_data_folder = sys.argv[1]
  else:
    raise ValueError("Training data folder is required")

  # Extract folder name from training_data_folder (e.g., "event-categories" from "training-data/event-categories")
  folder_name = os.path.basename(os.path.normpath(training_data_folder))

  output_dir = "./results"

  # Use the folder name for the ONNX output directory
  onnx_output_dir = os.path.join(output_dir, "onnx-models", folder_name)

  # Train the model
  train_classifier(training_data_folder=training_data_folder, output_dir=output_dir)

  # Convert to ONNX
  model_path = os.path.join(output_dir, "best_model")
  convert_to_onnx(model_path=model_path, output_path=onnx_output_dir)
