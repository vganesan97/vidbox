from bs4 import BeautifulSoup
import requests
import psycopg2
import logging
from datetime import datetime


def fetch_wikipedia_article(title):
    url = "https://en.wikipedia.org/w/api.php"
    params = {
        "action": "query",
        "format": "json",
        "titles": title,
        "prop": "extracts",
        "section": 3  # Get plain text instead of HTML
    }
    response = requests.get(url, params=params)
    data = response.json()
    page = next(iter(data["query"]["pages"].values()))
    return page.get("extract", "Article not found.")

def extract_plot(html_content, title, release_date):
    soup = BeautifulSoup(html_content, 'html.parser')

    # Locate the 'Plot' header
    plot_header = soup.find("span", id="Plot") or soup.find("span", id="Synopsis") or soup.find("span", id="Story")

    if plot_header is None:
        # Try fetching the article with "(film)" appended to the title
        article = fetch_wikipedia_article(title + " (film)")
        #logging.warning("trying film")
        soup = BeautifulSoup(article, 'html.parser')
        plot_header = soup.find("span", id="Plot")

        # Return None if still not found
        if plot_header is None:
            new_title = title + " (" + release_date +" film)"
            #print(new_title)
            #logging.warning("trying film + year")
            article = fetch_wikipedia_article(title + " (" + release_date +" film)")
            logging.info("trying film")
            soup = BeautifulSoup(article, 'html.parser')
            plot_header = soup.find("span", id="Plot")

            if plot_header is None: return ""


    plot_text = ""

    # Traverse through siblings until 'Cast' header is found
    for elem in plot_header.parent.find_next_siblings():
        if elem.find("span", id="Cast"):
            break
        plot_text += str(elem)

    plot_soup = BeautifulSoup(plot_text, 'html.parser')
    plot_text = plot_soup.get_text()


    return plot_text

def update_db_with_plot():
    conn = psycopg2.connect(
        database="vidbox-backend_development",
        host="localhost",
        user="vishaalganesan",
        password="vish",
        port="5433")

    cursor = conn.cursor()

    try:
        cursor.execute("SELECT title, release_date FROM movie_infos_top_rated")
        titles = cursor.fetchall()

        for title, release_date in titles:
            article = fetch_wikipedia_article(title)
            plot = extract_plot(article, title, str(release_date.year))
            if plot:
                cursor.execute(
                    "UPDATE movie_infos_top_rated SET plot = %s WHERE title = %s",
                    (plot, title)
                )
                conn.commit()
                logging.info(f"Updated plot for {title}")
            elif plot=="":
                logging.warning(f"Plot not found for {title}")
    finally:
        cursor.close()
        conn.close()

def fetch_titles_with_short_plot():
    conn = psycopg2.connect(
        database="vidbox-backend_development",
        host="localhost",
        user="vishaalganesan",
        password="vish",
        port="5433")
    cursor = conn.cursor()
    cursor.execute("SELECT id, title, release_date FROM movie_infos_top_rated WHERE LENGTH(plot) <= 2 OR plot IS NULL;")
    titles = cursor.fetchall()
    cursor.close()
    conn.close()
    with open('titles_with_short_plot.txt', 'w') as file:
        for id, title, release_date in titles:
            file.write(f"{id} {release_date.year} {title}\n")


def read_titles_and_process():
    parsed_data = list()
    with open('titles_with_short_plot.txt', 'r') as file:
        for line in file:
            parts = line.strip().split(' ', 2)  # Split by space, but only into 3 parts
            if len(parts) == 3:
                movie_id, year, title = parts
                parsed_data.append((
                    movie_id,
                    year,
                    title
                ))

    plot_count = 0
    for index, (id, year, title) in enumerate(parsed_data):
        try:
            wiki = fetch_wikipedia_article(title)
            plot = extract_plot(wiki, title, year)
            if len(plot) > 3:
                plot_count += 1
                print(f"plot count: {plot_count}, total movie count: {index}")
                conn = psycopg2.connect(
                    database="vidbox-backend_development",
                    host="localhost",
                    user="vishaalganesan",
                    password="vish",
                    port="5433")
                cursor = conn.cursor()
                cursor.execute(
                    "UPDATE movie_infos_top_rated SET plot = %s WHERE id = %s",
                    (plot, id)
                )
                conn.commit()
                cursor.close()
                conn.close()
                print(f"plot committed to db, movie id: {id}")
        finally:
            continue

# Usage
#update_db_with_plot()
fetch_titles_with_short_plot()
#read_titles_and_process()
