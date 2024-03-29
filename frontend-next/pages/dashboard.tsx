import { useRouter } from 'next/router';
import { useState, useEffect, useRef } from 'react';
import styles from 'styles/CreateAccount.module.css'
import { useAuthState } from 'react-firebase-hooks/auth';
import { Formik, Field, Form, ErrorMessage } from 'formik';
import { auth } from "@/firebase_creds";
import Movie from "@/components/Movie";
import SearchResultsList from "@/components/SearchResultsList";
import GroupList from "@/components/GroupList";
import SignOut from "@/components/SignOut";
import ImageComponent from "@/components/ImageComponent";
import {
    createGroupRequest,
    getFriendRequests,
    getFriendsRequest,
    getGroupAvatarPutSignedUrlRequest,
    getGroupsRequest,
    getLikedMoviesRequest,
    getProfileAvatarPutSignedUrlRequest,
    getPublicUsersNotFriendsRequest,
    getRecommendedMoviesRequest,
    putGroupAvatarRequest,
    putProfileAvatarRequest,
    refreshProfileAvatarSignedURLRequest,
    searchGroupsGetLastRequest,
    searchGroupsRequest,
    searchMoviesRequest
} from "@/requests/backendRequests";
import UserList from "@/components/UserList";
import User from "@/components/User";

export default function Dashboard() {
    const router = useRouter();
    const [username, setUsername] = useState<string | null>(null);
    const [user, loading, error] = useAuthState(auth)

    const [movieInfos, setMovieInfos] = useState([])
    const [groupInfos, setGroupInfos] = useState([])
    const [friendInfos, setFriendInfos] = useState([])
    const [friendRequests, setFriendRequests] = useState([])

    const [publicUsersInfos, setPublicUsersInfos] = useState([])


    const [likedMovies, setLikedMovies] = useState<Movie[]>([])
    const [recommendedMovies, setRecommendedMovies] = useState<Movie[]>([])


    const [searchQuery, setSearchQuery] = useState('');
    const [searchGroupsQuery, setSearchGroupsQuery] = useState('');

    const [signedURL, setSignedURL] = useState<string>('');
    const [groupSignedURL, setGroupSignedURL] = useState<string>('');

    const [groupAvatarFile, setGroupAvatarFile] = useState<File>()
    const [showCreateGroupForm, setShowCreateGroupForm] = useState(false);

    const [errorModalOpen, setErrorModalOpen] = useState(false);
    const [errorMsg, setErrorMsg] = useState({code: '', msg: ''})
    const userPrevious = useRef();

    interface CreateGroupFormValues {
        group_name: string;
        group_description?: string;
        privacy_level?: string;
        group_avatar?: string;
    }

    useEffect(() => {
        // If the user state changes
        if (user !== userPrevious.current) {
            // If the user state is not null
            if (user) {
                console.log(`logged in: ${user.uid}`);
                setUsername(user.email);
                console.log(`router query signed url: ${router.query.signedURL}`)
                if (typeof router.query.signedURL === 'string') setSignedURL(router.query.signedURL);
                console.log(`profile pic signed url: ${signedURL}`)
            }
            // Update the previous user state
            // @ts-ignore
            userPrevious.current = user;
        }
    }, [user]);


    const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setSearchQuery(event.target.value);
    };

    const handleSearchGroupsChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setSearchGroupsQuery(event.target.value);
    };

    const handleCreateGroupClick = async (event: React.MouseEvent) => {
        event.preventDefault()

        if (!user) {
            console.error("User is not authenticated");
            return;
        }
        setShowCreateGroupForm(true);
    }

    const getProfileAvatarPutSignedUrl = async (): Promise<string> => {
        const response = await getProfileAvatarPutSignedUrlRequest(user)
        if (!response) {
            throw new Error(`GET request failed: ${response}`);
        }
        console.log("signed url response", response.signedUrl);
        return response.signedUrl
    }

    const getGroupAvatarPutSignedUrl = async (groupId: number): Promise<string> => {
        const response = await getGroupAvatarPutSignedUrlRequest(user, groupId)
        if (!response) throw new Error(`GET request failed: ${response}`)
        console.log("signed url response", response.signedUrl);
        return response.signedUrl
    }

    async function handleUpdateProfileAvatar(event: React.ChangeEvent<HTMLInputElement>) {
        const file = event.target.files?.[0];
        // Check the file type before upload
        if (file != null && !file.type.startsWith('image/jpeg')) throw new Error('File is not an jpeg image');
        const signedUrl = await getProfileAvatarPutSignedUrl()
        console.log("su", signedUrl)
        await putProfileAvatarRequest(signedUrl, file as File)
        const res1 = await refreshProfileAvatarSignedURLRequest(user)
        console.log(res1)
        setSignedURL(res1.signedUrl)
    }

    const handleLikedMoviesClick = async (event: React.MouseEvent) => {
        event.preventDefault(); // prevent form submit
        console.log("Liked movies button clicked!"); // replace with actual implementation
        setMovieInfos([]); // Clear the current search results
        setRecommendedMovies([])
        setShowCreateGroupForm(false); // Hide the form
        const res = await getLikedMoviesRequest(user)
        console.log("liked movies request data: ", res)
        if (res) setLikedMovies(res); // Add the liked movies to the state
        else console.error(`Error: ${res}`);
    }

    const handleRecommendedMoviesClick = async (event: React.MouseEvent) => {
        event.preventDefault(); // prevent form submit
        console.log("Recommended movies button clicked!"); // replace with actual implementation
        setMovieInfos([]); // Clear the current search results
        setLikedMovies([])
        setShowCreateGroupForm(false); // Hide the form
        const res = await getRecommendedMoviesRequest(user)
        console.log("recommended movies request data: ", res)
        if (res) setRecommendedMovies(res); // Add the liked movies to the state
        else console.error(`Error: ${res}`);
    }

    const handleGetGroupsClick = async (event: React.MouseEvent) => {
        event.preventDefault(); // prevent form submit
        console.log("get groups button clicked!"); // replace with actual implementation
        setMovieInfos([]); // Clear the current search results
        setLikedMovies([])
        setRecommendedMovies([])
        setGroupInfos([])
        setShowCreateGroupForm(false); // Hide the form
        const response = await getGroupsRequest(user)
        if (response) {
            console.log('user groups:', response);
            setGroupInfos(response.content); // Add the liked movies to the state
        } else {
            console.error(`Error: ${response}`);
        }
    };

    const handleGetFriendsClick = async (event: React.MouseEvent) => {
        event.preventDefault(); // prevent form submit
        console.log("get friends button clicked!"); // replace with actual implementation
        setMovieInfos([]); // Clear the current search results
        setLikedMovies([])
        setRecommendedMovies([])
        setGroupInfos([])
        setPublicUsersInfos([])
        setFriendInfos([])
        setShowCreateGroupForm(false); // Hide the form
        const response = await getFriendsRequest(user)
        if (response) {
            console.log('friends:', response);
            setFriendInfos(response); // Add the liked movies to the state
        } else {
            console.error(`Error: ${response}`);
        }
    };

    const handleGetFriendRequestsClick = async (event: React.MouseEvent) => {
        event.preventDefault(); // prevent form submit
        console.log("get friends button clicked!"); // replace with actual implementation
        setMovieInfos([]); // Clear the current search results
        setLikedMovies([])
        setRecommendedMovies([])
        setGroupInfos([])
        setPublicUsersInfos([])
        setFriendInfos([])
        setShowCreateGroupForm(false); // Hide the form
        const response = await getFriendRequests(user)
        if (response) {
            console.log('friends:', response);
            setFriendRequests(response); // Add the liked movies to the state
        } else {
            console.error(`Error: ${response}`);
        }
    };

    const handleGetPublicUsersClick = async (event: React.MouseEvent) => {
        event.preventDefault(); // prevent form submit
        console.log("get public users button clicked!"); // replace with actual implementation

        // Clear the current UI states
        setMovieInfos([]);
        setLikedMovies([]);
        setRecommendedMovies([]);
        setGroupInfos([]);
        setPublicUsersInfos([]);
        setFriendInfos([]);
        setShowCreateGroupForm(false); // Hide the form

        // Fetch the public users not friends
        const response = await getPublicUsersNotFriendsRequest(user);

        if (response) {
            console.log('public users:', response);

            // Sort the response: users with a profilePic come first
            const sortedUsers = response.sort((a: any, b: any) => {
                // Convert the presence of profilePic to number explicitly for TypeScript
                const picA = a.profilePic ? 1 : 0;
                const picB = b.profilePic ? 1 : 0;

                // If both have profile pics or both do not, then do secondary sorting
                if (picA === picB) {
                    return a.id - b.id; // Assuming 'id' is a number, sort by id
                }

                // If picA is 1 and picB is 0, a should come first, hence return -1
                // If picA is 0 and picB is 1, b should come first, hence return 1
                return picB - picA;
            });

            setPublicUsersInfos(sortedUsers); // Update state with sorted users
        } else {
            console.error(`Error: ${response}`);
        }
    };

    const handleSearchSubmit = async (event: React.ChangeEvent<HTMLFormElement>) => {
        event.preventDefault();
        setGroupInfos([]);
        setLikedMovies([]); // Clear the current liked movies
        setRecommendedMovies([])
        setShowCreateGroupForm(false); // Hide the form
        const response = await searchMoviesRequest(user, searchQuery)
        if (!response) {
            console.error("Server response:", response);
            return;
        }
        console.log("data:", response);
        setMovieInfos(response.content)
    };

    const handleSearchGroupsSubmit = async (event: React.FormEvent<HTMLFormElement> | string) => {
        if (typeof event === 'string') {
            console.log("event", event)
            setSearchGroupsQuery(event as string)
            //setSearchGroupsQuery([event]); // Use the provided group name
            console.log("event type", typeof event)
            console.log("event search query", searchGroupsQuery)
        } else {
            event.preventDefault();
        }
        setGroupInfos([])
        setMovieInfos([])
        setLikedMovies([]); // Clear the current liked movies
        setRecommendedMovies([])
        setShowCreateGroupForm(false); // Hide the form
        if (typeof event === 'string') {
            console.log("type string")
            const response = await searchGroupsGetLastRequest(user, event)
            if (!response) {
                console.error("Server response:", response);
                return;
            }
            // @ts-ignore
            setGroupInfos([response])
        } else {
            const response = await searchGroupsRequest(user, searchGroupsQuery)
            if (!response) {
                console.error("Server response:", response);
                return;
            }
            setGroupInfos(response.content)
        }
        // @ts-ignore
        setGroupAvatarFile('')
    };

    async function handleCreateGroup(values: CreateGroupFormValues) {
        // Create the group without the avatar
        console.log(values, "vals")
        values.group_avatar = ""
        let groupId = -1
        let resj : any
        try {
            const res = await createGroupRequest(user, values)
            console.log("created group ", res)
            groupId = res.id
            resj = res
            // Clear the error message if there was one
            //setError(null);
            console.log("created group ", res.id)
            console.log("group avatar file", groupAvatarFile)
            console.log("create group form vals", JSON.stringify(values))
        } catch (error) {
            console.error('Failed to create group:', error);
            // Show an error message to the user
            //setError('Failed to create group. Please try again.');
            return;
        }

        if (groupAvatarFile) {
            // If the group creation was successful, proceed with uploading the avatar
            try {
                const signedUrl = await getGroupAvatarPutSignedUrl(groupId)
                await putGroupAvatarRequest(signedUrl, groupAvatarFile)
            } catch (error) {
                console.error('Failed to upload avatar:', error);
                // Show an error message to the user
                //setError('Failed to upload avatar. Please try again.');

                // Optionally, delete the group since the avatar upload failed
                //deleteGroup(groupId);
            }
        }
        // After successful group creation and avatar upload, refresh the group list.
        // Here, we're calling the search groups function with a fake event to simulate a form submission.
        await handleSearchGroupsSubmit(resj.groupName);
    }

    const shouldRenderRightComponents = () => {
        return showCreateGroupForm ||
            likedMovies.length > 0 ||
            movieInfos.length > 0 ||
            groupInfos.length > 0 ||
            friendInfos.length > 0 ||
            recommendedMovies.length > 0;
    }

if (!loading && user) {
        // @ts-ignore
    // @ts-ignore
    // @ts-ignore
    return (
            <div style={{display: 'flex'}}>
                <div style={shouldRenderRightComponents() ? {
                    borderRight: '3px solid white',
                    paddingRight: '8px',
                    paddingLeft: '5px',
                } : {}}
                >
                    <h1 style={{ paddingLeft: '5px'}}>{user.email}</h1>
                    <div style={{ paddingLeft:'5px', display: 'flex', alignItems: 'center', marginBottom: '10px' }}>
                        {signedURL.length > 0 ? (
                            <ImageComponent user={user} src={signedURL} alt={"Group Avatar"}/>
                        ) : (
                            <></>
                        )}
                        <div style={{ marginLeft: '10px' }}>
                            <h1>{router.query.firstName}</h1>
                            <h1>{router.query.lastName}</h1>
                        </div>
                    </div>
                    <div>
                        <h2 style={{ paddingLeft: '5px' }}>
                            <label
                                htmlFor="fileInput"
                                className="custom-file-upload"
                                style={{cursor: "pointer", textDecoration: "underline", color: "blue"}}
                            >
                                Upload a Profile Pic
                            </label>
                        </h2>
                        <input
                            type="file"
                            id="fileInput"
                            accept="image/jpeg"
                            onChange={handleUpdateProfileAvatar}
                            style={{display: 'none'}}/>
                    </div>
                    <form onSubmit={handleSearchSubmit} >
                        <input
                            type="text"
                            value={searchQuery}
                            onChange={handleSearchChange}
                            placeholder="Search for movies..."
                        />

                        <div>
                            <button type="submit">
                                Search for Movies
                            </button>
                        </div>
                        <div>
                            <button onClick={handleLikedMoviesClick}>
                                Liked Movies
                            </button>
                        </div>
                        <div>
                            <button onClick={handleRecommendedMoviesClick}>
                                Recommended Movies
                            </button>
                        </div>
                        <div>
                            <button onClick={handleCreateGroupClick}>
                                Create Group
                            </button>
                        </div>
                        <div>
                            <button onClick={handleGetGroupsClick}>
                                Groups
                            </button>
                        </div>
                        <div>
                            <button onClick={handleGetFriendsClick}>
                                Friends
                            </button>
                        </div>
                        <div>
                            <button onClick={handleGetFriendRequestsClick}>
                                Friend Requests
                            </button>
                        </div>
                        <div>
                            <button onClick={handleGetPublicUsersClick}>
                                Public Users
                            </button>
                        </div>
                    </form>

                    <form onSubmit={handleSearchGroupsSubmit}>
                        <input
                            type="text"
                            value={searchGroupsQuery}
                            onChange={handleSearchGroupsChange}
                            placeholder="Search for groups..."
                        />

                        <div>
                            <button type="submit">
                                Search for Groups
                            </button>
                        </div>

                        <SignOut/>
                    </form>

                </div>


                <div>
                    {showCreateGroupForm ? (
                        <></> // Do not show any search results list when the form is being shown
                    ) : (
                        <>
                            {likedMovies.length > 0 ? (
                                <>
                                    <h1 style={{paddingLeft: '0.6%', textDecoration: 'underline'}}>Liked Movies</h1>
                                    <SearchResultsList movies={likedMovies}/>
                                </>
                            ) : movieInfos.length > 0 ? (
                                <>
                                    <h1 style={{paddingLeft: '0.6%', textDecoration: 'underline'}}>{movieInfos.length > 0 ? 'Search Results' : ''}</h1>
                                    <SearchResultsList movies={movieInfos}/>
                                </>
                            ) : groupInfos.length > 0 ? (
                                <>
                                    <h1 style={{paddingLeft: '2%', textDecoration: 'underline'}}>Groups</h1>
                                    <GroupList groups={groupInfos}/>
                                </>
                            ) : friendInfos.length > 0 ? (
                                <>
                                    <h1 style={{paddingLeft: '2%', textDecoration: 'underline'}}>Friends</h1>
                                    <UserList users={friendInfos} isFriend={true} isFriendRequest={false}/>
                                </>
                            ) : publicUsersInfos.length > 0 ? (
                                <>
                                    <h1 style={{paddingLeft: '2%', textDecoration: 'underline'}}>Public Users</h1>
                                    <UserList users={publicUsersInfos} isFriend={false} isFriendRequest={false}/>
                                </>
                            ) : friendRequests.length > 0 ? (
                                <>
                                    <h1 style={{paddingLeft: '2%', textDecoration: 'underline'}}>Friend Requests</h1>
                                    <UserList users={friendRequests} isFriend={true} isFriendRequest={true}/>
                                </>
                            ) : recommendedMovies.length >0 ? (
                                <>
                                    <h1 style={{paddingLeft: '0.6%', textDecoration: 'underline'}}>Recommended Movies</h1>
                                    <SearchResultsList movies={recommendedMovies}/>
                                </>
                            ) : null}
                        </>
                    )}
                </div>


                <div>
                {showCreateGroupForm ? (
                    // Render your form here. You might want to create a new Formik form similar to the one in your login page.
                    <div>

                            <Formik
                                initialValues={{
                                    group_name: '',
                                    group_description: '',
                                    privacy: 'public',
                                    group_avatar: undefined
                                }}
                                validateOnChange={false}
                                validateOnBlur={false}
                                validate={(values) => {
                                    const errors: Partial<CreateGroupFormValues> = {};
                                    if (!values.group_name) errors.group_name = 'Required';
                                    return errors;
                                }}
                                onSubmit={
                                    async (values, { setSubmitting }) => {

                                        await handleCreateGroup(values);
                                        setSubmitting(false);
                                    }
                                }
                            >
                                {({ isSubmitting, setFieldValue }) => (
                                    <Form style={{ marginRight: '500px' }}>
                                        <div>
                                            <div className={styles.title}>Create Group</div>
                                            <label className={styles.label} htmlFor="group_name">Group Name</label>
                                            <Field
                                                placeholder="Group Name"
                                                id="group_name"
                                                name="group_name"
                                                required />
                                            <ErrorMessage name="group_name" component="div" />
                                        </div>

                                        <div>
                                            <label className={styles.label} htmlFor="group_description">Group Description</label>
                                            <Field
                                                id="group_description"
                                                name="group_description"
                                                placeholder="Group Description"
                                            />
                                            <ErrorMessage name="group_description" component="div" />
                                        </div>

                                        <div>
                                            <label className={styles.label} htmlFor="privacy">Privacy Level</label>
                                            <Field style={{width: '40%', height: '30px', }} as="select" name="privacy">
                                                <option value="public">Public</option>
                                                <option value="private">Private</option>
                                            </Field>
                                            <ErrorMessage name="privacy" component="div" className={styles.error} />
                                        </div>

                                        <div>
                                            <label
                                                className={styles.label}
                                                htmlFor="group_avatar"
                                            >
                                                Group Avatar
                                            </label>
                                            <input
                                                id="group_avatar"
                                                name="group_avatar"
                                                type="file"
                                                onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                                                    if (event.currentTarget.files && event.currentTarget.files.length > 0) {
                                                        const file = event.currentTarget.files[0];
                                                        setGroupAvatarFile(file)
                                                        //setFieldValue('group_avatar', file);
                                                    } else {
                                                        //setFieldValue('group_avatar', null);
                                                    }
                                                }}
                                            />
                                            <ErrorMessage name="group_avatar" component="div" />
                                        </div>

                                        <div className={styles.signupWrapper}>
                                            <button type="submit" disabled={isSubmitting}>
                                                {isSubmitting ? "Loading..." : "Submit"}
                                            </button>
                                        </div>

                                    </Form>
                                )}
                            </Formik>
                    </div>


                ) : (
                    <>
                        {/* The rest of your component's content goes here */}
                    </>
                )}
                </div>

            </div>
        );
    } else if (loading) {
        return (
            <div>
                <h1>loading..</h1>
            </div>
        )
    } else {
        return (
            <div>
                <h1>signed out</h1>
            </div>
        )
    }
}
