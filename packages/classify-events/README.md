# DistilBERT Text Classification Project

This project provides a complete setup for training and using a DistilBERT model to classify short texts into categories.

## Setup

### Using uv

```bash
# Install uv (if not already installed)
curl -LsSf https://astral.sh/uv/install.sh | sh

# Create virtual environment and install dependencies
uv venv
source .venv/bin/activate  # On Windows: .venv\Scripts\activate
uv pip install -r requirements.txt

# Run the pipeline
python main.py
```
