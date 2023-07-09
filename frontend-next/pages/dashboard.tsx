import { useRouter } from 'next/router';
import { useState, useEffect } from 'react';
import { useAuthState } from 'react-firebase-hooks/auth';
import { auth } from "@/firebase_creds";

type Movie = {
    id: number;
    posterPath: string;
    backdropPath: string;
    overview: string;
    title: string;
    releaseDate: number;
    movieId: number;
    liked: boolean;
}

type MovieProps = {
    movie: Movie;
};

type SearchResultsListProps = {
    movies: Movie[];
}

function Movie({ movie }: MovieProps) {

    var imgUrl = 'https://image.tmdb.org/t/p/original/'

    const [isHovered, setIsHovered] = useState(false);
    const [isLiked, setIsLiked] = useState(movie.liked);
    const [user, loading, error] = useAuthState(auth)

    const handleLike = async (event: React.MouseEvent) => {
        event.stopPropagation();
        if (!user) {
            console.error("User is not authenticated");
            return;
        }
        try {
            const idToken = await user.getIdToken(true);
            const response = await fetch('http://127.0.0.1:8081/movies/like-movie', {
                method: 'POST',
                headers: {
                    'Authorization': 'Bearer ' + idToken,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({movieId: movie.id})
            });

            const res = await response.json()
            console.log("login response", res)

            if (response.ok) {
                res.liked ? setIsLiked(true) : setIsLiked(false);
                console.log(`Movie ${movie.id} ${res.liked ? 'liked' : 'unliked'} by user ${user.uid}`);
            } else {
                console.error("Error liking/unliking movie:", response.status, response.statusText);
            }
        } catch (e) {
            console.log("Error ")
        }
    };

    const handleClick = () => {
        console.log(`Post ${movie.id} clicked!`);
    }

    const movieStyle = {
        cursor: 'pointer', // Changes the cursor to a hand when hovering over the div
        marginBottom: '10px',
        backgroundColor: isHovered ? '#444444' : ''// Add some margin between the movies
    };

    return (
        <div onClick={handleClick}
             style={movieStyle}
             onMouseEnter={() => setIsHovered(true)}
             onMouseLeave={() => setIsHovered(false)}>
            <div style={{ display: 'flex', alignItems: 'center' }}>
                <h2 style={{ marginRight: '10px' }}>{movie.title}{` (${movie.releaseDate})`}</h2>
                <button
                    onClick={(event: React.MouseEvent) => handleLike(event)}
                    className="like-button">{isLiked ? '❤️' : '♡'}️</button>
            </div>
            <img src={`${imgUrl}${movie.posterPath}`}
                 alt={movie.title}
                 style={{width: "200px", height: "300px"}}/>
            <p><b>{movie.overview}</b></p>
        </div>
    );
}

function SearchResultsList({ movies }: SearchResultsListProps) {
    return (
        <div>
            {movies.map((movie: Movie) => (
                // Pass the entire post object as a prop to the Post component
                <Movie key={movie.id} movie={movie} />
            ))}
        </div>
    );
}
export default function Dashboard() {
    const router = useRouter();
    const [username, setUsername] = useState<string | null>(null);
    const [movieInfos, setMovieInfos] = useState([])
    const [searchQuery, setSearchQuery] = useState('');
    const [user, loading, error] = useAuthState(auth)
    const [likedMovies, setLikedMovies] = useState<Movie[]>([]);
    const [idToken, setIdToken] = useState<string | null>(null); // add this line


    useEffect(() => {
        if (router.query.username) {
            setUsername(router.query.username as string);
        }
    }, [router.query.username, user]);

    // useEffect(() => {
    //     const getToken = async () => {
    //         if (user) {
    //             const token = await user.getIdToken(true);
    //             setIdToken(token); // set the idToken
    //         }
    //     }
    //     getToken().catch(error => console.error('Error getting token:', error));
    // }, [user]);

    const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setSearchQuery(event.target.value);
    };

    const handleLikedMoviesClick = async (event: React.MouseEvent) => {
        event.preventDefault(); // prevent form submit
        console.log("Liked movies button clicked!"); // replace with actual implementation

        setMovieInfos([]); // Clear the current search results

        if (!user) {
            console.error("User is not authenticated");
            return;
        }
        const idToken = await user.getIdToken(true);
        const response = await fetch('http://127.0.0.1:8081/movies/liked-movies', {
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + idToken,
                'Content-Type': 'application/json'
            }
        })
        if (response.ok) {
            const res = await response.json();
            console.log(`Liked movies for user: ${user.email}`, res);
            setLikedMovies(res); // Add the liked movies to the state
        } else {
            console.error(`Error: ${response.status}`);
        }
    };

    const handleSearchSubmit = async (event: React.ChangeEvent<HTMLFormElement>) => {
        event.preventDefault();
        setLikedMovies([]); // Clear the current liked movies

        if (!user) {
            console.error("User is not authenticated");
            return;
        }
        const idToken = await user.getIdToken(true);

        const response = await fetch(`http://127.0.0.1:8081/movies/search-movies?query=${searchQuery}`,{
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + idToken,
                'Content-Type': 'application/json'
            }
        });
        if (!response.ok) {
            console.error("Server response:", response.status, response.statusText);
            return;
        }
        const data = await response.json();
        console.log("data:", data);
        setMovieInfos(data.content)
    };


    if (!loading && user) {
        return (
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <div style={{ marginRight: '20px' }}>
                    <h1>
                        {user.email}
                    </h1>
                    <form onSubmit={handleSearchSubmit}>
                        <input
                            type="text"
                            value={searchQuery}
                            onChange={handleSearchChange}
                            placeholder="Search for movies..."
                        />
                        <div>
                            <button type="submit">Search</button>
                        </div>
                        <div>
                            <button onClick={handleLikedMoviesClick}>
                                Liked Movies
                            </button>
                        </div>
                    </form>
                </div>

                <div>
                    {likedMovies.length > 0 ? (
                        <>
                            <h1>Liked Movies</h1>
                            <SearchResultsList movies={likedMovies}/>
                        </>
                    ) : (
                        <>
                            <h1>{movieInfos.length > 0 ? 'Search Results' : ''}</h1>
                            <SearchResultsList movies={movieInfos}/>
                        </>
                    )}
                </div>

            </div>
        );
    } else {
        return (
            <div>
                <h1>loading..</h1>
            </div>
        )
    }
}
