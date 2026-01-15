#!/usr/bin/env python3
"""
Convert safetensors model to ONNX format for use with @huggingface/transformers.js

This script converts a DistilBERT model saved in safetensors format to ONNX format,
which can be used with the transformers.js library in JavaScript/TypeScript environments.
"""

import os
import sys
from pathlib import Path

# Try to import optimum, provide helpful error if not available
try:
  from optimum.onnxruntime import ORTModelForSequenceClassification
except ImportError:
  print("Error: optimum package not installed.")
  print("Please install it with:")
  print("  pip install 'optimum[onnxruntime]'")
  print("\nOr install from requirements.txt:")
  print("  pip install -r requirements.txt")
  sys.exit(1)

from transformers import AutoTokenizer


def convert_to_onnx(model_path: str = "./results/best_model", output_path: str = "./results/best_model_onnx"):
  """
  Convert safetensors model to ONNX format.

  Args:
      model_path: Path to the safetensors model directory
      output_path: Path where the ONNX model will be saved
  """
  # Check if model path exists
  if not os.path.exists(model_path):
    print(f"Error: Model path '{model_path}' does not exist.")
    sys.exit(1)

  # Check if config.json exists
  config_path = os.path.join(model_path, "config.json")
  if not os.path.exists(config_path):
    print(f"Error: config.json not found in '{model_path}'.")
    sys.exit(1)

  print(f"Converting model from {model_path} to ONNX format...")
  print(f"Output directory: {output_path}")
  print("-" * 60)

  try:
    # Load the model and convert to ONNX
    print("Loading model and converting to ONNX...")
    model = ORTModelForSequenceClassification.from_pretrained(
      model_path,
      export=True,  # This triggers the conversion to ONNX
    )

    # Create output directory if it doesn't exist
    os.makedirs(output_path, exist_ok=True)

    # Save the ONNX model
    print(f"Saving ONNX model to {output_path}...")
    model.save_pretrained(output_path)
    print("✓ ONNX model saved successfully")

    # Copy the tokenizer files
    print("Copying tokenizer files...")
    tokenizer = AutoTokenizer.from_pretrained(model_path)
    tokenizer.save_pretrained(output_path)
    print("✓ Tokenizer files copied")

    # Copy config.json if not already copied
    import shutil
    config_dest = os.path.join(output_path, "config.json")
    if not os.path.exists(config_dest):
      shutil.copy2(config_path, config_dest)
      print("✓ Config file copied")

    print("-" * 60)
    print(f"✓ Conversion complete! ONNX model saved to: {output_path}")
    print("\nYou can now use this model with @huggingface/transformers.js:")
    print(f"  import {{ pipeline }} from '@huggingface/transformers';")
    print(f"  const classifier = await pipeline('text-classification', '{output_path}');")

  except ImportError as e:
    print(f"Error: Required package not installed. Please install optimum:")
    print("  pip install 'optimum[onnxruntime]'")
    print("\nOr install from requirements.txt:")
    print("  pip install -r requirements.txt")
    sys.exit(1)
  except Exception as e:
    print(f"Error during conversion: {e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)
