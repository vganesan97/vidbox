import React, { useState } from 'react';
import ImageLoader from './ImageLoader';
import {refreshProfileAvatarSignedURLRequest} from "@/requests/backendRequests";

interface ImageComponentProps {
    src: string;
    alt: string;
    user: any;
    fromMovie?: any;
}
const ImageComponent: React.FC<ImageComponentProps> = ({ user, src, alt, fromMovie}) => {
    const [loaded, setLoaded] = useState(false);
    const [signedURL, setSignedURL] = useState<string>('');

    let attempts = 0
    const handleRefreshProfileAvatarSignedURL = async () => {
        console.log("refresh")
        if (attempts >= 3) {  // only try to refresh the URL up to 3 times
            console.error('Failed to load image after 3 attempts');
            attempts = 0
            return;
        }
        setSignedURL(await refreshProfileAvatarSignedURLRequest(user))
    }

    return (
        <>
            <img
                src={src}
                alt={alt}
                style={{
                    display: loaded ? "block" : "none",
                    width: fromMovie ? "200px" : "100px",
                    height: fromMovie ? "300px" : "100px",
                    boxShadow: '0 0 0 0 #fff', // Change color to match your background
                    borderRadius: '5%', // Adjust this value for the amount of roundness you want at the corners
                }}
                onLoad={() => setLoaded(true)}
                onError={handleRefreshProfileAvatarSignedURL}
            />
            {!loaded && <ImageLoader fromMovie={fromMovie}/>}

        </>
    );
};

export default ImageComponent;
