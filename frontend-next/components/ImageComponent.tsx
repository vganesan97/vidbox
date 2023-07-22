import React, { useState } from 'react';
import ImageLoader from './ImageLoader';

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

        if (!user) {
            console.error("User is not authenticated");
            return;
        }

        const idToken = await user.getIdToken(true);
        const response = await fetch(`http://127.0.0.1:8081/avatar/user/get-signed-url`, {
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + idToken
            }
        });

        const res = await response.json()
        console.log("su", res.signedUrl)
        setSignedURL(res.signedUrl)
    }

    return (
        <>
            <img
                src={src}
                alt={alt}
                style={{
                    display: loaded ? "block" : "none",
                    width: fromMovie ? "200px" : "100px",
                    height: fromMovie ? "300px" : "100px"
                }}
                onLoad={() => setLoaded(true)}
                onError={handleRefreshProfileAvatarSignedURL}
            />
            {!loaded && <ImageLoader fromMovie={fromMovie}/>}
        </>
    );
};

export default ImageComponent;
