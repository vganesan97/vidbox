import { useEffect, useState } from 'react';
import Movie from "@/components/Movie";

function useAllImagesLoaded(movies: Movie[]): boolean {
    const [allImagesLoaded, setAllImagesLoaded] = useState<boolean>(false);

    var imgUrl = 'https://image.tmdb.org/t/p/original/'

    useEffect(() => {
        let loadedCounter = 0;
        const imageCount = movies.length;

        const imageLoaded = () => {
            loadedCounter += 1;
            if (loadedCounter === imageCount) {
                setAllImagesLoaded(true);
            }
        };

        movies.forEach(movie => {
            const img = new window.Image();
            img.onload = imageLoaded;
            img.src = `${imgUrl}${movie.poster_path}`;
        });
    }, [movies]);

    return allImagesLoaded;
}

export default useAllImagesLoaded;
