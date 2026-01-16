# fastText Document Classification

This project provides a complete setup for training and using a fastText model to classify short texts into
categories. fastText is faster and produces smaller models compared to transformer-based approaches.

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
python main.py training-data/event-categories
```

## Testing

After training a model, you can test it using pytest:

```bash
# Install dev dependencies (includes pytest)
uv pip install -e ".[dev]"

# Run all tests
pytest

# Run tests with verbose output
pytest -v

# Run a specific test
pytest tests/test_classifier.py::TestClassifier::test_predict_example_texts
```

The test suite includes:
- Model loading tests
- Single and batch prediction tests
- Example text prediction tests
- Edge case handling (empty strings, etc.)
- Validation of prediction structure and confidence scores

Alternatively, you can use the standalone test script:

```bash
# Test with default paths (auto-detects model in results directory)
python test_model.py

# Or specify custom paths
python test_model.py ./results/models/event-categories/best_model.ftz ./results/models/event-categories/labels.txt
```
