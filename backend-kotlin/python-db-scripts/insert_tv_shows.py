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

def make_request_tv(page):
    for i in range(1, page):
        response = requests.get('https://api.themoviedb.org/3/tv/top_rated/', params=params(i))

        data = response.json()
        results = data.get('results', [])
        for result in results:
            cur = conn.cursor()

            data1 = {
                'backdrop_path': result.get('backdrop_path'),
                'first_air_date': result['first_air_date'] if ('first_air_date' in result and result['first_air_date']) else None,
                'id': result.get('id'),
                'name': result.get('name'),
                'original_language': result.get('original_language'),
                'original_name': result.get('original_name'),
                'overview': result.get('overview'),
                'popularity': result.get('popularity'),
                'poster_path': result.get('poster_path'),
                'vote_average': result.get('vote_average'),
                'vote_count': result.get('vote_count')
            }

            # Execute the INSERT statement
            cur.execute("""
                INSERT INTO tv_infos_top_rated
                (
                    backdrop_path, first_air_date, tv_id, name,
                    original_language, original_name, overview, popularity,
                    poster_path, vote_average, vote_count
                )
                VALUES
                (
                    %(backdrop_path)s, %(first_air_date)s, %(id)s, %(name)s,
                    %(original_language)s, %(original_name)s, %(overview)s, %(popularity)s,
                    %(poster_path)s, %(vote_average)s, %(vote_count)s
                )
                RETURNING id
            """, data1)
            new_id = cur.fetchone()[0]
            # Commit the changes
            conn.commit()

            # After inserting the tv show, insert the genres
            for genre_id in result.get('genre_ids', []):
                cur.execute("""
                        SELECT id FROM genres
                        WHERE genre_id = %(genre_id)s
                    """, {'genre_id': genre_id})

                genre = cur.fetchone()

                if genre:
                    cur.execute("""
                        INSERT INTO tv_genres (tv_id, genre_id)
                        VALUES (%(tv_id)s, %(genre_id)s)
                        ON CONFLICT (tv_id, genre_id) DO NOTHING
                    """, {'tv_id': new_id, 'genre_id': genre[0]})

            # And insert the origin countries
            for country in result.get('origin_country', []):
                cur.execute("""
                    INSERT INTO tv_origin_countries (tv_id, country_code)
                    VALUES (%(tv_id)s, %(country_code)s)
                    ON CONFLICT (tv_id, country_code) DO NOTHING
                """, {'tv_id': new_id, 'country_code': country})

            conn.commit()

make_request_tv(148)
