import json

def read_and_verify_dimensions(file_path, expected_dimensions=1536):
    with open(file_path, 'r') as file:
        data = json.load(file)

    for key, value in data.items():
        embedding = value[0]['embedding']
        dimensions_count = len(embedding)
        if dimensions_count != expected_dimensions:
            print(f"Embedding with key {key} has {dimensions_count} dimensions instead of {expected_dimensions}.")
            return False

    print(f"All embeddings have {expected_dimensions} dimensions.")
    return True

# Read and verify dimensions
file_path = "embeddings2.json"
read_and_verify_dimensions(file_path)
