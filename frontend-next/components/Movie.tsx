import {auth} from "@/firebase_creds";
import { useState } from 'react';
import { useAuthState } from 'react-firebase-hooks/auth';
import ImageComponent from "@/components/ImageComponent";
import ErrorModal from "@/components/ErrorModal";
import {ErrorMessage, Field, Form, Formik} from 'formik';
import {createReviewRequest, likeMovieRequest} from "@/requests/backendRequests";


type Movie = {
    id: number;
    poster_path: string;
    backdrop_path: string;
    overview: string;
    title: string;
    release_date: number;
    movie_id: number;
    liked: boolean;
    reviewContent: string;
}

type MovieProps = {
    movie: Movie;
};

interface FormValues {
    reviewContent: string
}

const Movie = ({ movie }: MovieProps) => {

    var imgUrl = 'https://image.tmdb.org/t/p/original/'

    const [isHovered, setIsHovered] = useState(false);
    const [isLiked, setIsLiked] = useState(movie.liked);
    const [user, loading, error] = useAuthState(auth)
    const [isFlipped, setIsFlipped] = useState(false);
    const [showForm, setShowForm] = useState(false);
    const [errorMsg, setErrorMsg] = useState({code: '', msg: ''})
    const [errorModalOpen, setErrorModalOpen] = useState(false);

    const handleFormClick = (event: React.MouseEvent) => {
        event.stopPropagation(); // Prevent the click event from bubbling up
    };

    const handleLike = async (event: React.MouseEvent) => {
        event.stopPropagation();
        if (!user) {
            console.error("User is not authenticated");
            return;
        }
        try {
            const res = await likeMovieRequest(user, movie.id)
            if (res) {
                res.liked ? setIsLiked(true) : setIsLiked(false);
                console.log(`Movie ${movie.id} ${res.liked ? 'liked' : 'unliked'} by user ${user.uid}`);
            } else {
                console.error("Error liking/unliking movie:", res.status, res.statusText);
            }
        } catch (e) {
            console.log("Error ")
        }
    };

    const handleClick = () => {
        console.log(`Movie ${movie.id} clicked!`);
        console.log(`${imgUrl}${movie.backdrop_path}`)
        setIsFlipped(!isFlipped);
        setShowForm(!showForm);
    }

    const movieStyle = {
        cursor: 'pointer', // Changes the cursor to a hand when hovering over the div
        marginBottom: '10px',
        backgroundColor: isHovered ? '#444444' : '',// Add some margin between the movies
        backgroundImage: `url(${imgUrl}${movie.backdrop_path})`,
    };

    // const formStyle: React.CSSProperties = {
    //     position: 'absolute', // Absolutely position the form
    //     right: '2%',
    //     top: '20%', // Align it to the top of the container
    //     left: '20%', // Align it to the right of the container
    //     background: 'rgba(0, 0, 0, 0.7)', // Semi-transparent black background over the image
    //     display: showForm ? 'flex' : 'none', // Show or hide the form
    //     padding: '10px',
    //     color: '#fff',
    //     // Add other styles for the form
    // };

    const formStyle: React.CSSProperties = {
        width: '80%', // Take up 80% of the container's width
        top: '20%', // Align it to the top of the container
        left: '20%',
        background: 'rgba(0, 0, 0, 0.7)',
        color: '#fff',
        display: showForm ? 'flex' : 'none', // Show or hide the form
        flexDirection: 'column', // Stack the form elements vertically
        padding: '10px',
        borderRadius: '5px', // Add some rounding to the corners
        // Other styles
    };


    const formStyle1: React.CSSProperties = {
        position: 'absolute',
        width: '80%', // Take up 80% of the container's width
        top: '20%', // Align it to the top of the container
        left: '20%',
        background: 'rgba(0, 0, 0, 0.7)',
        color: '#fff',
        display:'flex', // Show or hide the form
        flexDirection: 'column', // Stack the form elements vertically
        padding: '10px',
        borderRadius: '5px', // Add some rounding to the corners
        // Other styles
    };

    const containerStyle: React.CSSProperties = {
        position: 'relative', // Set to relative so the absolute positioning of the form is relative to this container
        display: 'flex', // Use flexbox to align movie poster and form side by side
        cursor: 'pointer',
        marginBottom: '10px',
        backgroundImage: `url(${imgUrl}${movie.backdrop_path})`, // Set the background image for the whole container
        //backgroundSize: showForm ? 'cover' : 'auto', // Cover the entire container if the form is showing
    };

    const createReview = async (values: any) => {
        console.log(values)
        try {
            await createReviewRequest(user, values)
        } catch (error: any) {
            setErrorModalOpen(true);
            setErrorMsg({
                code: '',
                msg: error.message,
            });
        }
    };

    return (
        <div style={containerStyle} onClick={handleClick}>
            <div style={movieStyle}>
                <div style={{ display: 'flex', alignItems: 'center', background: 'linear-gradient(to bottom, rgba(0, 0, 0, 0.7), rgba(0, 0, 0, 0))', color: '#fff', padding: '10px' }}>
                    <h2 style={{marginTop: '-15px'}}>
                        {movie.title}{` (${new Date(movie.release_date).toLocaleDateString(undefined, { year: 'numeric', month: 'long', day: 'numeric' })})`}
                    </h2>
                    <button
                        onClick={(event: React.MouseEvent) => handleLike(event)}
                        className="like-button"
                        style={{ background: 'transparent', border: 'none', color: 'white', marginTop: '-8px' }}>{isLiked ? '❤️' : '♡'}️
                    </button>
                </div>
                <div style={{paddingLeft: '10px', marginTop: '-13px', display: 'flex'}}>
                    <ImageComponent
                        user={{}}
                        src={`${imgUrl}${movie.poster_path}`}
                        alt={movie.title}
                        fromMovie={true}
                    />
                </div>
                <div style={{
                    paddingLeft: '10px',
                    background: 'linear-gradient(to top, rgba(0, 0, 0, 1), rgba(0, 0, 0, 0.2))',
                    color: '#fff',
                    padding: '10px',
                }}>
                    <b>{movie.overview}</b>
                </div>
            </div>
            {/*<div style={formStyle1}>*/}
            {/*    <b>{movie.reviewContent ? movie.reviewContent : ''}</b>*/}
            {/*</div>*/}
            <div style={formStyle} onClick={handleFormClick}>
                <Formik
                    initialValues={{ reviewContent: movie.reviewContent ? movie.reviewContent : '', movieId: movie.id }}
                    validateOnChange={false} // Only validate the form when the submit button is clicked
                    validateOnBlur={false}
                    validate={values => {
                        const errors: Partial<FormValues> = {};
                        if (!values.reviewContent) {
                            errors.reviewContent = 'Required';
                        }
                        return errors;
                    }}
                    onSubmit={
                        async (values, { setSubmitting }) => {
                            await createReview(values)
                            setSubmitting(false)
                        }}
                >
                    {({ isSubmitting}) => (
                        <Form>
                            <div>
                                <label style={{ fontSize: '25px', height: '30px', fontWeight: 'bold' }} htmlFor="reviewContent">Review</label>
                                {/*<Field as="textarea" id="reviewContent" name="reviewContent" rows="10" cols="100" />*/}
                                <Field
                                    as="textarea"
                                    id="reviewContent"
                                    name="reviewContent"
                                    rows="10"
                                    cols="100"
                                    style={{
                                        fontFamily: 'Arial, sans-serif',
                                        fontSize: '16px',
                                        padding: '10px',
                                        width: '100%',
                                        height: '100px',
                                        border: '1px solid #ccc',
                                        borderRadius: '5px',
                                        resize: 'vertical'
                                    }}
                                />
                                <ErrorMessage name="reviewContent" component="div" />
                            </div>

                            <div>
                                <button type="submit" disabled={isSubmitting}>
                                    {loading ? "Loading..." : "Submit"}
                                </button>
                            </div>

                            <div style={{width: '100%'}}>
                                {errorModalOpen && <ErrorModal error={errorMsg}/>}
                            </div>
                        </Form>
                    )}
                </Formik>
            </div>
        </div>
    );
}
export default Movie