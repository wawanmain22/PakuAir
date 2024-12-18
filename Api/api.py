from flask import Flask, jsonify, request, render_template_string
import numpy as np
import joblib
import os
import time
import platform
from typing import Dict, Any, Union
from http import HTTPStatus
import warnings
warnings.filterwarnings('ignore')

def is_windows():
    return platform.system().lower() == "windows"

print(f"Starting server initialization on {platform.system()}...")
start_time = time.time()

# Import timeout handler based on OS
if not is_windows():
    import signal
    from contextlib import contextmanager
    
    @contextmanager
    def timeout(seconds):
        def handler(signum, frame):
            raise TimeoutError("Model loading timed out")
        
        # Set timeout handler
        signal.signal(signal.SIGALRM, handler)
        signal.alarm(seconds)
        
        try:
            yield
        finally:
            signal.alarm(0)
else:
    from contextlib import contextmanager
    import threading
    import _thread
    
    @contextmanager
    def timeout(seconds):
        timer = threading.Timer(seconds, lambda: _thread.interrupt_main())
        timer.start()
        try:
            yield
        except KeyboardInterrupt:
            raise TimeoutError("Model loading timed out")
        finally:
            timer.cancel()

# Constants and Configurations
SCRIPT_DIR = os.path.dirname(os.path.realpath(__file__))
MODEL_PATH = os.path.join(SCRIPT_DIR, 'water_potability.pkl')
OPTIMIZED_MODEL_PATH = os.path.join(SCRIPT_DIR, 'water_potability_optimized.joblib')
TIMEOUT_SECONDS = 60

# HTML template for home page
HOME_TEMPLATE = """
<!DOCTYPE html>
<html>
<head>
    <title>Water Potability Prediction API</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
            background-color: #f5f5f5;
        }
        .container {
            background-color: white;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        h1 {
            color: #2c3e50;
            text-align: center;
        }
        .feature-list {
            background-color: #f8f9fa;
            padding: 15px;
            border-radius: 4px;
            margin: 15px 0;
        }
        .endpoint {
            background-color: #e9ecef;
            padding: 10px;
            border-radius: 4px;
            margin: 10px 0;
        }
        .status {
            text-align: center;
            padding: 10px;
            margin: 10px 0;
            border-radius: 4px;
        }
        .ready {
            background-color: #d4edda;
            color: #155724;
        }
        .not-ready {
            background-color: #f8d7da;
            color: #721c24;
        }
        code {
            background-color: #f8f9fa;
            padding: 2px 4px;
            border-radius: 4px;
            font-family: monospace;
        }
        .example {
            background-color: #f8f9fa;
            padding: 15px;
            border-radius: 4px;
            margin: 15px 0;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🚰 Water Potability Prediction API</h1>
        <div class="status {% if model_ready %}ready{% else %}not-ready{% endif %}">
            Status: {% if model_ready %}Model Ready - API Siap Digunakan{% else %}Model Not Ready - Please Contact Administrator{% endif %}
        </div>
        
        <p>Selamat datang di API Prediksi Kualitas Air Minum. API ini menggunakan model machine learning untuk memprediksi kelayakan air minum berdasarkan berbagai parameter kualitas air.</p>
        
        <h2>Cara Penggunaan:</h2>
        <div class="endpoint">
            <strong>Endpoint:</strong> POST /predict
        </div>
        
        <h3>Parameter Input:</h3>
        <div class="feature-list">
            <ul>
                <li><code>ph</code>: Tingkat pH air (0-14)</li>
                <li><code>Hardness</code>: Tingkat kesadahan air (mg/L)</li>
                <li><code>Solids</code>: Total padatan terlarut (ppm)</li>
                <li><code>Chloramines</code>: Konsentrasi kloramin (ppm)</li>
                <li><code>Sulfate</code>: Konsentrasi sulfat (mg/L)</li>
                <li><code>Conductivity</code>: Konduktivitas (μS/cm)</li>
                <li><code>Organic_carbon</code>: Karbon organik total (ppm)</li>
                <li><code>Trihalomethanes</code>: Konsentrasi trihalometan (μg/L)</li>
                <li><code>Turbidity</code>: Tingkat kekeruhan (NTU)</li>
            </ul>
        </div>

        <h3>Contoh Request:</h3>
        <div class="example">
            <code>
POST http://localhost:5000/predict<br>
Content-Type: application/json<br>
<br>
{<br>
    "ph": 7.5,<br>
    "Hardness": 145.3,<br>
    "Solids": 378.9,<br>
    "Chloramines": 7.2,<br>
    "Sulfate": 223.8,<br>
    "Conductivity": 447.2,<br>
    "Organic_carbon": 11.3,<br>
    "Trihalomethanes": 66.4,<br>
    "Turbidity": 3.9<br>
}
            </code>
        </div>
        
        <h3>Contoh Response:</h3>
        <div class="example">
            <code>
{<br>
    "potability": 1,<br>
    "message": "Air layak minum",<br>
    "prediction_time": "0.0021 seconds"<br>
}
            </code>
        </div>
        
        <div class="system-info">
            <p><strong>System Information:</strong></p>
            <p>Running on: {{ platform }}</p>
            <p>Python version: {{ python_version }}</p>
        </div>
    </div>
</body>
</html>
"""

# Initialize Flask app
app = Flask(__name__)
app.config['JSON_SORT_KEYS'] = False
app.config['PROPAGATE_EXCEPTIONS'] = True
app.debug = False

def load_model():
    """Load ML model with cross-platform support."""
    global model, model_ready
    model_ready = False
    model = None
    
    try:
        # Print diagnostic information
        print(f"\nSystem Information:")
        print(f"Operating System: {platform.system()}")
        print(f"Python Version: {platform.python_version()}")
        print(f"Current working directory: {os.getcwd()}")
        print(f"Script directory: {SCRIPT_DIR}")
        print(f"Model path exists: {os.path.exists(MODEL_PATH)}")
        
        if os.path.exists(MODEL_PATH):
            size_mb = os.path.getsize(MODEL_PATH) / (1024 * 1024)
            print(f"Model file size: {size_mb:.2f} MB")
        
        # Try loading optimized model with timeout
        try:
            if os.path.exists(OPTIMIZED_MODEL_PATH):
                print("Loading pre-optimized model...")
                with timeout(TIMEOUT_SECONDS):
                    model = joblib.load(OPTIMIZED_MODEL_PATH)
            else:
                print("Loading and optimizing model for first time...")
                with timeout(TIMEOUT_SECONDS):
                    with open(MODEL_PATH, 'rb') as f:
                        model = joblib.load(f)
                    
                    print("Saving optimized model...")
                    joblib.dump(model, OPTIMIZED_MODEL_PATH, compress=3)
        except TimeoutError:
            print("Warning: Model loading timed out, trying without timeout...")
            if not model:
                with open(MODEL_PATH, 'rb') as f:
                    model = joblib.load(f)
        
        if model is not None:
            print("Model loaded successfully!")
            model_ready = True
        else:
            raise ValueError("Model failed to load (is None)")
            
    except Exception as e:
        print(f"Error loading model: {str(e)}")
        print(f"Error type: {type(e)}")

# Load model at startup
load_model()

print(f"\nServer initialization completed in {time.time() - start_time:.2f} seconds")
print(f"Model status: {'Ready' if model_ready else 'Not Ready'}")

def validate_input(data: Dict[str, float]) -> Union[str, None]:
    """Validate input parameters."""
    required_features = ['ph', 'Hardness', 'Solids', 'Chloramines', 'Sulfate',
                        'Conductivity', 'Organic_carbon', 'Trihalomethanes', 'Turbidity']
    
    if not data:
        return "No input data provided"
    
    if not isinstance(data, dict):
        return f"Invalid input type. Expected dict, got {type(data)}"
    
    for feature in required_features:
        if feature not in data:
            return f"Missing required feature: {feature}"
        
        if not isinstance(data[feature], (int, float)):
            return f"Invalid value type for {feature}: must be numeric, got {type(data[feature])}"
        
        if isinstance(data[feature], float):
            if np.isnan(data[feature]):
                return f"Invalid value for {feature}: NaN not allowed"
            if np.isinf(data[feature]):
                return f"Invalid value for {feature}: Infinity not allowed"
    
    return None

@app.route('/')
def home() -> str:
    """Render home page with API documentation."""
    return render_template_string(
        HOME_TEMPLATE,
        model_ready=model_ready,
        platform=platform.system(),
        python_version=platform.python_version()
    )

@app.route('/predict', methods=['POST'])
def predict() -> Union[Dict[str, Any], tuple[Dict[str, str], int]]:
    """Handle prediction requests."""
    if not model_ready:
        return {
            "error": "Model not ready",
            "status": "Service unavailable",
            "details": "The model failed to load properly. Please contact administrator."
        }, HTTPStatus.SERVICE_UNAVAILABLE

    try:
        data = request.get_json()
        if not data:
            return {"error": "No input data provided"}, HTTPStatus.BAD_REQUEST
        
        validation_error = validate_input(data)
        if validation_error:
            return {"error": validation_error}, HTTPStatus.BAD_REQUEST
        
        input_features = [
            data['ph'], data['Hardness'], data['Solids'],
            data['Chloramines'], data['Sulfate'], data['Conductivity'],
            data['Organic_carbon'], data['Trihalomethanes'], data['Turbidity']
        ]
        
        prediction_start = time.time()
        input_query = np.array([input_features])
        result = model.predict(input_query)[0]
        prediction_time = time.time() - prediction_start
        
        response = {
            'potability': int(result),
            'message': 'Air layak minum' if result == 1 else 'Air tidak layak minum',
            'prediction_time': f"{prediction_time:.4f} seconds"
        }
        
        print(f"Prediction made in {prediction_time:.4f} seconds")
        return response
    
    except Exception as e:
        error_details = {
            "error": "Prediction failed",
            "type": str(type(e).__name__),
            "details": str(e)
        }
        return error_details, HTTPStatus.INTERNAL_SERVER_ERROR

if __name__ == '__main__':
    print("\nStarting production server...")
    from waitress import serve
    print("Server is running on http://127.0.0.1:5000")
    serve(app, host='127.0.0.1', port=5000, threads=4)