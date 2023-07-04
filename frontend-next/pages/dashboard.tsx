import { useRouter } from 'next/router';
import { useState, useEffect } from 'react';
import { useAuthState } from 'react-firebase-hooks/auth';
import {auth} from "@/firebase_creds";

export default function Dashboard() {
    const router = useRouter();
    const [username, setUsername] = useState<string | null>(null);
    const [searchQuery, setSearchQuery] = useState('');
    const [user, loading, error] = useAuthState(auth)

    useEffect(() => {
        if (router.query.username) {
            setUsername(router.query.username as string);
        }
    }, [router.query.username, user]);

    const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setSearchQuery(event.target.value);
    };

    const handleSearchSubmit = async (event: React.ChangeEvent<HTMLFormElement>) => {
        event.preventDefault();
        // Assume this function queries an API and returns a list of movies
        // const movies = await searchMovies(searchQuery);
        // console.log(movies);
    };

    if (!loading && user) {
        return (
            <div>
                <h1>
                    Welcome,
                    {username}:
                    {user.displayName},
                    {user.metadata.creationTime}
                </h1>
                <form onSubmit={handleSearchSubmit}>
                    <input
                        type="text"
                        value={searchQuery}
                        onChange={handleSearchChange}
                        placeholder="Search for movies..."
                    />
                    <button type="submit">Search</button>
                </form>
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
