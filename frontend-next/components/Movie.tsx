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
            // const idToken = await user.getIdToken(true);
            // const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/movies/like-movie`, {
            //     method: 'POST',
            //     headers: {
            //         'Authorization': 'Bearer ' + idToken,
            //         'Content-Type': 'application/json'
            //     },
            //     body: JSON.stringify({movieId: movie.id})
            // });
            //
            // const res = await response.json()
            // console.log("login response", res)
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
        backgroundImage: `url(${imgUrl}${movie.backdrop_path})`
    };

    // return (
    //     <div onClick={handleClick}
    //          style={movieStyle}
    //          onMouseEnter={() => setIsHovered(true)}
    //          onMouseLeave={() => setIsHovered(false)}>
    //         <div style={{ display: 'flex', alignItems: 'center', background: 'linear-gradient(to bottom, rgba(0, 0, 0, 0.7), rgba(0, 0, 0, 0))', color: '#fff', padding: '10px' }}>
    //             <h2 style={{ marginRight: '10px' }}>
    //                 {movie.title}{` (${new Date(movie.release_date).toLocaleDateString(undefined, { year: 'numeric', month: 'long', day: 'numeric' })})`}
    //             </h2>
    //             <button
    //                 onClick={(event: React.MouseEvent) => handleLike(event)}
    //                 className="like-button"
    //                 style={{ background: 'transparent', border: 'none', color: 'white' }}>{isLiked ? '❤️' : '♡'}️
    //             </button>
    //         </div>
    //
    //         <ImageComponent
    //             user={{}}
    //             src={`${imgUrl}${movie.poster_path}`}
    //             alt={movie.title}
    //             fromMovie={true}
    //         />
    //
    //         {/*<img src={`${imgUrl}${movie.poster_path}`}*/}
    //         {/*     alt={movie.title}*/}
    //         {/*     style={{width: "200px", height: "300px"}}/>*/}
    //         <div style={{ background: 'linear-gradient(to top, rgba(0, 0, 0, 1), rgba(0, 0, 0, 0.2))', color: '#fff', padding: '10px' }}>
    //             <b>{movie.overview}</b>
    //         </div>
    //
    //     </div>
    // );

    // return (
    //     <div className={`${styles.flipContainer} ${isFlipped ? styles.flipped : ""}`} onClick={handleClick} style={movieStyle}>
    //         <div className={styles.flipper}>
    //             <div onMouseEnter={() => setIsHovered(true)} onMouseLeave={() => setIsHovered(false)}>
    //                 <div style={{ display: 'flex', alignItems: 'center', background: 'linear-gradient(to bottom, rgba(0, 0, 0, 0.7), rgba(0, 0, 0, 0))', color: '#fff', padding: '10px' }}>
    //                     <h2 style={{ marginRight: '10px' }}>
    //                         {movie.title}{` (${new Date(movie.release_date).toLocaleDateString(undefined, { year: 'numeric', month: 'long', day: 'numeric' })})`}
    //                     </h2>
    //                     <button
    //                         onClick={(event: React.MouseEvent) => handleLike(event)}
    //                         className="like-button"
    //                         style={{ background: 'transparent', border: 'none', color: 'white' }}>{isLiked ? '❤️' : '♡'}️
    //                     </button>
    //                 </div>
    //                 <ImageComponent
    //                     user={{}}
    //                     src={`${imgUrl}${movie.poster_path}`}
    //                     alt={movie.title}
    //                     fromMovie={true}
    //                 />
    //                 <div style={{ background: 'linear-gradient(to top, rgba(0, 0, 0, 1), rgba(0, 0, 0, 0.2))', color: '#fff', padding: '10px' }}>
    //                     <b>{movie.overview}</b>
    //                 </div>
    //             </div>
    //             <div className={styles.back}>
    //                 <form>
    //                     <input type="text" placeholder="Your Name" />
    //                     <input type="email" placeholder="Your Email" />
    //                     <textarea placeholder="Your Message"></textarea>
    //                     <button type="submit">Send</button>
    //                 </form>
    //             </div>
    //         </div>
    //     </div>
    // );

    // const formStyle: React.CSSProperties = {
    //     display: showForm ? 'block' : 'none', // Show or hide the form
    //     background: 'rgba(0, 0, 0, 0.7)',
    //     padding: '10px',
    //     color: '#fff',
    //     // Add other styles for the form
    // };

    const formStyle: React.CSSProperties = {
        position: 'absolute', // Absolutely position the form
        right: '2%',
        top: '20%', // Align it to the top of the container
        left: '20%', // Align it to the right of the container
        background: 'rgba(0, 0, 0, 0.7)', // Semi-transparent black background over the image
        display: showForm ? 'block' : 'none', // Show or hide the form
        padding: '10px',
        color: '#fff',
        // Add other styles for the form
    };

    const inputStyle = {
        width: '100%',
        padding: '10px',
        margin: '5px 0',
        backgroundColor: 'rgba(255, 255, 255, 0.1)',
        color: '#fff',
        border: '1px solid #fff'
    };

    const buttonStyle = {
        padding: '10px',
        backgroundColor: '#444',
        color: '#fff',
        border: 'none',
        cursor: 'pointer',
        margin: '5px'
    };

    // if (showForm) {
    //     return (
    //         <div style={movieStyle}>
    //             <form style={formStyle}>
    //                 <input type="text" placeholder="Your Name" style={inputStyle}/>
    //                 <input type="email" placeholder="Your Email" style={inputStyle}/>
    //                 <textarea placeholder="Your Message" style={inputStyle}></textarea>
    //                 <button type="submit" style={buttonStyle}>Send</button>
    //                 <button type="button" onClick={handleClick} style={buttonStyle}>Go back</button>
    //             </form>
    //         </div>
    //     );
    // }

    const formContainerStyle = {
        position: 'absolute',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        background: 'rgba(0, 0, 0, 0.7)', // Semi-transparent background
        display: showForm ? 'flex' : 'none', // Show or hide the form
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center'
    };

    // return (
    //     <div onClick={handleClick} style={movieStyle}>
    //         <div style={{ display: 'flex', alignItems: 'center', background: 'linear-gradient(to bottom, rgba(0, 0, 0, 0.7), rgba(0, 0, 0, 0))', color: '#fff', padding: '10px' }}>
    //             <h2 style={{ marginRight: '10px' }}>
    //                 {movie.title}{` (${new Date(movie.release_date).toLocaleDateString(undefined, { year: 'numeric', month: 'long', day: 'numeric' })})`}
    //             </h2>
    //             <button
    //                 onClick={(event: React.MouseEvent) => handleLike(event)}
    //                 className="like-button"
    //                 style={{ background: 'transparent', border: 'none', color: 'white' }}>{isLiked ? '❤️' : '♡'}️
    //             </button>
    //         </div>
    //         <ImageComponent
    //             user={{}}
    //             src={`${imgUrl}${movie.poster_path}`}
    //             alt={movie.title}
    //             fromMovie={true}
    //         />
    //         <div style={{ background: 'linear-gradient(to top, rgba(0, 0, 0, 1), rgba(0, 0, 0, 0.2))', color: '#fff', padding: '10px' }}>
    //             <b>{movie.overview}</b>
    //         </div>
    //     </div>
    // );

    // const containerStyle = {
    //     display: 'flex', // Use flexbox to align movie poster and form side by side
    //     flexDirection: showForm ? 'row' : 'column', // Change direction based on showForm
    //     cursor: 'pointer',
    //     marginBottom: '10px',
    //     backgroundColor: isHovered ? '#444444' : '',
    // };

    const containerStyle: React.CSSProperties = {
        position: 'relative', // Set to relative so the absolute positioning of the form is relative to this container
        //display: 'flex', // Use flexbox to align movie poster and form side by side
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
        // try {
        //     if (user) {
        //         let idToken = await user.getIdToken(true);
        //         const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/review/create`, {
        //             method: 'POST',
        //             headers: {
        //                 'Authorization': 'Bearer ' + idToken,
        //                 'Content-Type': 'application/json' },
        //             body: JSON.stringify(values),
        //         });
        //
        //         setErrorMsg({
        //             code: '',
        //             msg: ''
        //         });
        //     }
        //
        // } catch (error) {
        //     // This error will be handled below
        //     console.error("An error occurred:", error);
        // }

        // if (error) {
        //     setErrorModalOpen(true);
        //     setErrorMsg({
        //         code: '',
        //         msg: error.message,
        //     });
        // }
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
                <div style={{paddingLeft: '10px', marginTop: '-13px'}}>
                    <ImageComponent
                        user={{}}
                        src={`${imgUrl}${movie.poster_path}`}
                        alt={movie.title}
                        fromMovie={true}
                    />
                </div>
                <div style={{ paddingLeft: '10px', background: 'linear-gradient(to top, rgba(0, 0, 0, 1), rgba(0, 0, 0, 0.2))', color: '#fff', padding: '10px' }}>
                    <b>{movie.overview}</b>
                </div>
                {/* ... existing movie rendering code, including poster, title, like button, and overview ... */}
            </div>
            <div style={formStyle} onClick={handleFormClick}>
                <Formik
                    initialValues={{ reviewContent: '', movieId: movie.id }}
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
                                <Field as="textarea" id="reviewContent" name="reviewContent" rows="10" cols="100" />
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
                {/*<form>*/}
                {/*    <input type="text" placeholder="Your Name" style={inputStyle} />*/}
                {/*    <input type="email" placeholder="Your Email" style={inputStyle} />*/}
                {/*    <textarea placeholder="Your Message" style={inputStyle}></textarea>*/}
                {/*    <button type="submit" style={buttonStyle}>Send</button>*/}
                {/*    <button type="button" onClick={handleClick} style={buttonStyle}>Close</button>*/}
                {/*</form>*/}
            </div>
        </div>
    );
}
export default Movie