import psycopg2
import requests
from google.cloud import secretmanager


conn = psycopg2.connect(
        database="vidbox-backend_development",
        host="localhost",
        user="vishaalganesan",
        password="vish",
        port="5433")

client = secretmanager.SecretManagerServiceClient()
name = f"projects/vidbox-7d2c1/secrets/moviedb-api-key/versions/latest"
response = client.access_secret_version(request={"name": name})

def params(pageNum):
    return {
        "api_key": response.payload.data.decode("UTF-8"),
        "page": pageNum
    }

def insert_test_users():
    response = requests.get('https://randomuser.me/api/?nat=us&results=500')
    data = response.json()
    test_users =[
        {
            'first_name': result['name']['first'],
            'last_name': result['name']['last'],
            'email': result['email'],
            'password': result['login']['password'],
            'username': result['login']['username'],
            'date_of_birth': result['dob']['date'],
            'latitude': result['location']['coordinates']['latitude'],
            'longitude': result['location']['coordinates']['longitude']
        }
        for result in data['results']
    ]

    for user in test_users:
        cur = conn.cursor()
        data1 = {
            'first_name': user['first_name'],
            'last_name': user['last_name'],
            'email': user['email'],
            'password': user['password'],
            'username': user['username'],
            'date_of_birth': user['date_of_birth'],
            'latitude': user['latitude'],
            'longitude': user['longitude']
        }

        cur.execute(
            'INSERT INTO test_users (first_name, last_name, email, password, username, date_of_birth, latitude, longitude) VALUES (%(first_name)s, %(last_name)s, %(email)s, %(password)s, %(username)s, %(date_of_birth)s, %(latitude)s, %(longitude)s)',
            data1
        )

        conn.commit()


#insert_test_users()
