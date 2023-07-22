import ContentLoader from 'react-content-loader'

// @ts-ignore
const ImageLoader = ({ fromMovie }) => {
    return (
        <ContentLoader
            speed={2}
            width={400}
            height={300}
            backgroundColor="#f3f3f3"
            foregroundColor="#ecebeb"
            style={{
                width: fromMovie ? "200px" : "100px",
                height: fromMovie ? "300px" : "100px"
            }}
        >
            <rect x="0" y="0" rx="5" ry="5" width="400" height="300" />
        </ContentLoader>
    );
}
export default ImageLoader