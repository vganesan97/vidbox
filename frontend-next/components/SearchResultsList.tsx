import React from 'react';
import Movie from "@/components/Movie";
import useAllImagesLoaded from "@/hooks/useAllImagesLoaded";

interface SearchResultsListProps {
    movies: Movie[];
}

const SearchResultsList: React.FC<SearchResultsListProps> = ({ movies }) => {
    const allImagesLoaded = useAllImagesLoaded(movies);

    // if (!allImagesLoaded) {
    //     return <div>Loading...</div>; // or return null or a spinner component
    // }

    return (
        <div>
            {movies.map(movie => (
                <Movie key={movie.id} movie={movie} />
            ))}
        </div>
    );
}

export default SearchResultsList;
