import Movie from "@/components/Movie";

type SearchResultsListProps = {
    movies: Movie[];
}

const SearchResultsList = ({ movies }: SearchResultsListProps) => {
    return (
        <div>
            {movies.map((movie: Movie) => (
                // Pass the entire post object as a prop to the Post component
                <Movie key={movie.id} movie={movie}/>
            ))}
        </div>
    );
}

export default SearchResultsList