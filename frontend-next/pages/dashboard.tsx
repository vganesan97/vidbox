import { useRouter } from 'next/router';
import { useState, useEffect, useRef } from 'react';
import { useAuthState, useSignOut } from 'react-firebase-hooks/auth';
import { auth } from "@/firebase_creds";


type Movie = {
    id: number;
    poster_path: string;
    backdrop_path: string;
    overview: string;
    title: string;
    release_date: number;
    movie_id: number;
    liked: boolean;
}

type MovieProps = {
    movie: Movie;
};

type SearchResultsListProps = {
    movies: Movie[];
}

function SignOut() {
    const [signOut, loading, error] = useSignOut(auth);
    const router = useRouter();


    if (error) {
        return (
            <div>
                <p>Error: {error.message}</p>
            </div>
        );
    }
    if (loading) {
        return <p>Loading...</p>;
    }
    return (
        <div className="App">
            <button
                onClick={async () => {
                    const success = await signOut();
                    if (success) {
                        router.push('/');
                    }
                }}
            >
                Sign out
            </button>
        </div>
    );
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
        console.log(`Movie ${movie.id} clicked!`);
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
                <h2 style={{ marginRight: '10px' }}>{movie.title}{` (${movie.release_date})`}</h2>
                <button
                    onClick={(event: React.MouseEvent) => handleLike(event)}
                    className="like-button">{isLiked ? '❤️' : '♡'}️</button>
            </div>
            <img src={`${imgUrl}${movie.poster_path}`}
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

    const userPrevious = useRef();

    useEffect(() => {
        // If the user state changes
        if (user !== userPrevious.current) {
            // If the user state is not null
            if (user) {
                console.log(`logged in: ${user.uid}`);
                setUsername(user.email);
            }
            // Update the previous user state
            // @ts-ignore
            userPrevious.current = user;
        }
    }, [user]);


    const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setSearchQuery(event.target.value);
    };

    const handleCreateGroupClick = (event: React.MouseEvent) => {
        event.preventDefault()
    }

    const getSignedUrl = async (): Promise<string> => {
        if (user == null) {
            console.error("user not logged in or authorized");
            throw new Error("user not logged in or authorized");
        }

        const idToken = await user.getIdToken(true);
        const response = await fetch(`http://127.0.0.1:8081/avatar/user`, {
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + idToken
            }
        });

        if (!response.ok) {
            throw new Error(`GET request failed: ${response.status}`);
        }

        const responseStr = await response.json();
        console.log("signed url response", responseStr.signedUrl);

        return responseStr.signedUrl
    }


    async function handleUpdateProfileAvatar(event: React.ChangeEvent<HTMLInputElement>) {
        const file = event.target.files?.[0];

        // Check the file type before upload
        if (file != null && !file.type.startsWith('image/jpeg')) {
            throw new Error('File is not an jpeg image');
        }

        const signedUrl = await getSignedUrl()
        const response = await fetch(signedUrl, {
            method: 'PUT',
            body: file,
            headers: {
                'Content-Type': 'image/jpeg' // Important: the content type should match the one you specified when generating the signed URL
            }
        });

        if (!response.ok) {
            throw new Error(`Upload failed: ${response.status}`);
        }
    }


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

    // const handleLogOut = async (event: React.MouseEvent) => {
    //     event.preventDefault();
    //     setLikedMovies([]); // Clear the current liked movies
    //
    //     if (!user) {
    //         console.error("User is not authenticated");
    //         return;
    //     }
    //     const idToken = await user.getIdToken(true);
    //     const response = await fetch(`http://127.0.0.1:8081/movies/search-movies?query=${searchQuery}`,{
    //         method: 'GET',
    //         headers: {
    //             'Authorization': 'Bearer ' + idToken,
    //             'Content-Type': 'application/json'
    //         }
    //     });
    //     if (!response.ok) {
    //         console.error("Server response:", response.status, response.statusText);
    //         return;
    //     }
    //     const data = await response.json();
    //     console.log("data:", data);
    //     setMovieInfos(data.content)
    // };


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
                            <button onClick={handleCreateGroupClick}>
                                Create Group
                            </button>
                        </div>
                        <div>
                            <h2>
                                <label
                                    htmlFor="fileInput"
                                    className="custom-file-upload"
                                    style={{cursor: "pointer", textDecoration: "underline", color: "blue"}}
                                >
                                    Upload a Profile Pic
                                </label>
                            </h2>
                            <input
                                type="file"
                                id="fileInput"
                                accept="image/jpeg"
                                onChange={handleUpdateProfileAvatar}
                                style={{display: 'none'}}/>
                        </div>
                        <div>
                            <button onClick={handleLikedMoviesClick}>
                                Liked Movies
                            </button>
                        </div>
                        <SignOut/>
                        {/*<div>*/}
                        {/*    <button onClick={handleLogOut}>*/}
                        {/*        Log Out*/}
                        {/*    </button>*/}
                        {/*</div>*/}
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
    } else if (loading) {
        return (
            <div>
                <h1>loading..</h1>
            </div>
        )
    } else {
        return (
            <div>
                <h1>signed out</h1>
            </div>
        )
    }
}
