import {auth} from "@/firebase_creds";
import { useState } from 'react';
import { useAuthState } from 'react-firebase-hooks/auth';

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

const Movie = ({ movie }: MovieProps) => {

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
export default Movie