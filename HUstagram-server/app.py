from flask import Flask, request, jsonify
from flask_pymongo import PyMongo
import os
from datetime import datetime
from bson import ObjectId

# Initialize Flask app
app = Flask(__name__, static_folder='images')

# MongoDB connection string (updated with correct cluster and password)
app.config["MONGO_URI"] = "mongodb+srv://abarakati:KQzqQ8GFKJNsYWDp@hustagramcluster.6qdqw6k.mongodb.net/HUstagram?retryWrites=true&w=majority&appName=HUstagramCluster"
mongo = PyMongo(app)

# Set upload folder
UPLOAD_FOLDER = 'images'
if not os.path.exists(UPLOAD_FOLDER):
    os.makedirs(UPLOAD_FOLDER)

# Home route
@app.route('/')
def home():
    return "HUstagram server is running!"

# Upload image and store comment in MongoDB
@app.route('/upload', methods=['POST'])
def upload_image():
    if 'image' not in request.files or 'comment' not in request.form:
        return jsonify({'error': 'Image and comment are required'}), 400

    image = request.files['image']
    comment = request.form['comment']

    if image.filename == '':
        return jsonify({'error': 'No selected file'}), 400

    # Generate unique filename
    filename = datetime.now().strftime("%Y%m%d%H%M%S") + "_" + image.filename
    filepath = os.path.join(UPLOAD_FOLDER, filename)
    image.save(filepath)

    # Create a dictionary to store in MongoDB
    post_data = {
        'filename': filename,
        'comment': comment,
        'timestamp': datetime.utcnow()
    }

    try:
        # Access the 'posts' collection in the 'HUstagram' database
        result = mongo.db['posts'].insert_one(post_data)

        return jsonify({
            'message': 'Image uploaded and saved to MongoDB',
            'id': str(result.inserted_id),
            'filename': filename,
            'comment': comment
        }), 200

    except Exception as e:
        return jsonify({'error': f'Failed to insert image into MongoDB: {str(e)}'}), 500

# Run the server
if __name__ == '__main__':
    print("MongoDB connected successfully!")
    app.run(debug=True)
