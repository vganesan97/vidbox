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


def make_request_movie(page):
    for i in range(1, page):
        response = requests.get('https://api.themoviedb.org/3/movie/top_rated/', params=params(i))
        data = response.json()
        results = data.get('results', [])
        for result in results:
            cur = conn.cursor()
            data1 = {
                'poster_path': result.get('poster_path'),
                'backdrop_path': result.get('backdrop_path'),
                'overview': result.get('overview'),
                'title': result.get('title'),
                'release_date': result['release_date'] if ('release_date' in result and result['release_date']) else None,
                'id': result.get('id'),
                'adult': result.get('adult'),
                'original_language': result.get('original_language'),
                'original_title': result.get('original_title'),
                'popularity': result.get('popularity'),
                'video': result.get('video'),
                'vote_average': result.get('vote_average'),
                'vote_count': result.get('vote_count')
            }

            # Execute the INSERT statement
            cur.execute("""
                INSERT INTO movie_infos_top_rated
                (
                    poster_path, backdrop_path, overview, title,
                    release_date, movie_id, adult, original_language,
                    original_title, popularity, video, vote_average, vote_count
                )
                VALUES
                (
                    %(poster_path)s, %(backdrop_path)s, %(overview)s, %(title)s,
                    %(release_date)s, %(id)s, %(adult)s, %(original_language)s,
                    %(original_title)s, %(popularity)s, %(video)s, %(vote_average)s, %(vote_count)s
                )
                RETURNING id
            """, data1)
            new_id = cur.fetchone()[0]
            # Commit the changes
            conn.commit()

            # After inserting the movie, insert the genres
            for genre_id in result.get('genre_ids', []):
                cur.execute("""
                        SELECT id FROM genres
                        WHERE genre_id = %(genre_id)s
                    """, {'genre_id': genre_id})

                genre = cur.fetchone()

                if genre:
                    cur.execute("""
                        INSERT INTO movie_genres (movie_id, genre_id)
                        VALUES (%(movie_id)s, %(genre_id)s)
                        ON CONFLICT (movie_id, genre_id) DO NOTHING
                    """, {'movie_id': new_id, 'genre_id': genre[0]})

            conn.commit()

make_request_movie(568)
