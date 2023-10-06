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

def get_and_insert_genres():
    # URLs for movie and TV genres
    movie_genre_url = 'https://api.themoviedb.org/3/genre/movie/list'
    tv_genre_url = 'https://api.themoviedb.org/3/genre/tv/list'

    # Get genres for movies
    response = requests.get(movie_genre_url, params=params(1))
    movie_genres = response.json()['genres']

    # Get genres for TV shows
    response = requests.get(tv_genre_url, params=params(1))
    tv_genres = response.json()['genres']

    # Combine both lists
    all_genres = movie_genres + tv_genres

    # Remove duplicates (genres with the same id)
    genres_by_id = {genre['id']: genre for genre in all_genres}
    unique_genres = list(genres_by_id.values())
    print(unique_genres)
    for genre in unique_genres:
        cur = conn.cursor()
        genre_data = {
            'id': genre['id'],
            'name': genre['name']
        }

        # Execute the INSERT statement with ON CONFLICT DO NOTHING to avoid inserting duplicates
        cur.execute("""
            INSERT INTO genres (genre_id, genre_name)
            VALUES (%(id)s, %(name)s)
            ON CONFLICT (genre_id) DO NOTHING
        """, genre_data)

        # Commit the changes
        conn.commit()

# Call the function to get and insert genres
get_and_insert_genres()
