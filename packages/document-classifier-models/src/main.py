import os
import sys

from train import train_classifier

if __name__ == "__main__":
  # Parse command-line arguments
  # Usage: python main.py [training_data_folder] [output_dir]

  if len(sys.argv) > 1:
    training_data_folder = sys.argv[1]
  else:
    raise ValueError("Training data folder is required")

  # Extract folder name from training_data_folder (e.g., "event-categories" from "training-data/event-categories")
  folder_name = os.path.basename(os.path.normpath(training_data_folder))

  # Use the folder name for the output directory
  output_dir = os.path.join("./results", folder_name)

  # Train the model
  train_classifier(training_data_folder=training_data_folder, output_dir=output_dir)
