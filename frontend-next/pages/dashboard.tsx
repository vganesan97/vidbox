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

export default function Dashboard() {
    const router = useRouter();
    const [username, setUsername] = useState<string | null>(null);
    const [user, loading, error] = useAuthState(auth)

    const [movieInfos, setMovieInfos] = useState([])
    const [groupInfos, setGroupInfos] = useState([])
    const [likedMovies, setLikedMovies] = useState<Movie[]>([])

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
        if (user == null) {
            console.error("user not logged in or authorized");
            throw new Error("user not logged in or authorized");
        }

        const idToken = await user.getIdToken(true);
        const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/avatar/user/put-signed-url`, {
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + idToken
            }
        });

        if (!response.ok) {
            throw new Error(`GET request failed: ${response.status}`);
        }

        const responseStr = await response.json();
        console.log("signed url response", responseStr.signedUrl);

        return responseStr.signedUrl
    }

    const getGroupAvatarGetSignedUrl = async (groupInfoId: Number): Promise<string> => {
        if (user == null) {
            console.error("user not logged in or authorized");
            throw new Error("user not logged in or authorized");
        }

        //const idToken = await user.getIdToken(true);
        const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/avatar/group/${groupInfoId}/get-signed-url`, {
            method: 'GET'
        });
        const res = await response.json()


        if (!response.ok) {
            throw new Error(`GET request failed: ${response.status}`);
        }

        const responseStr = await response.json();
        console.log("signed url response", responseStr.signedUrl);

        return responseStr.signedUrl
    }

    const getGroupAvatarPutSignedUrl = async (groupId: Number): Promise<string> => {
        if (!user) {
            console.error("user not logged in or authorized");
            throw new Error("user not logged in or authorized");
        }

        const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/avatar/group/${groupId}/put-signed-url`, {
            method: 'GET',
        });

        if (!response.ok) {
            throw new Error(`GET request failed: ${response.status}`);
        }

        const responseStr = await response.json();
        console.log("signed url response", responseStr.signedUrl);

        return responseStr.signedUrl
    }

    async function handleUpdateProfileAvatar(event: React.ChangeEvent<HTMLInputElement>) {
        const file = event.target.files?.[0];

        // Check the file type before upload
        if (file != null && !file.type.startsWith('image/jpeg')) {
            throw new Error('File is not an jpeg image');
        }

        const signedUrl = await getProfileAvatarPutSignedUrl()
        const response = await fetch(signedUrl, {
            method: 'PUT',
            body: file,
            headers: {
                'Content-Type': 'image/jpeg' // Important: the content type should match the one you specified when generating the signed URL
            }
        });

        if (!response.ok) {
            throw new Error(`Upload failed: ${response.status}`);
        }

        if (!user) {
            console.error("User is not authenticated");
            return;
        }
        const idToken = await user.getIdToken(true);
        const response1 = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/avatar/user/get-signed-url`, {
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + idToken
            }
        });
        const res1 = await response1.json()
        console.log(res1)
        setSignedURL(res1.signedUrl)
    }

    const handleLikedMoviesClick = async (event: React.MouseEvent) => {
        event.preventDefault(); // prevent form submit
        console.log("Liked movies button clicked!"); // replace with actual implementation

        setMovieInfos([]); // Clear the current search results
        setShowCreateGroupForm(false); // Hide the form

        if (!user) {
            console.error("User is not authenticated");
            return;
        }
        const idToken = await user.getIdToken(true);
        const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/movies/liked-movies`, {
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + idToken,
                'Content-Type': 'application/json'
            }
        })
        if (response.ok) {
            const res = await response.json();
            console.log(`Liked movies for user: ${user.email}`, res);
            setLikedMovies(res); // Add the liked movies to the state
        } else {
            console.error(`Error: ${response.status}`);
        }
    };

    const handleGetGroupsClick = async (event: React.MouseEvent) => {
        event.preventDefault(); // prevent form submit
        console.log("get groups button clicked!"); // replace with actual implementation

        setMovieInfos([]); // Clear the current search results
        setLikedMovies([])
        setGroupInfos([])
        setShowCreateGroupForm(false); // Hide the form

        if (!user) {
            console.error("User is not authenticated");
            return;
        }
        const idToken = await user.getIdToken(true);
        const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/group/get-groups`, {
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + idToken,
                'Content-Type': 'application/json'
            }
        })
        if (response.ok) {
            const res = await response.json();
            console.log('user groups:', res);
            setGroupInfos(res.content); // Add the liked movies to the state
        } else {
            console.error(`Error: ${response.status}`);
        }
    };

    const handleSearchSubmit = async (event: React.ChangeEvent<HTMLFormElement>) => {
        event.preventDefault();
        setGroupInfos([]);
        setLikedMovies([]); // Clear the current liked movies
        setShowCreateGroupForm(false); // Hide the form

        if (!user) {
            console.error("User is not authenticated");
            return;
        }

        const idToken = await user.getIdToken(true);
        const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/movies/search-movies?query=${searchQuery}`, {
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + idToken,
                'Content-Type': 'application/json'
            }
        });
        if (!response.ok) {
            console.error("Server response:", response.status, response.statusText);
            return;
        }
        const data = await response.json();
        console.log("data:", data);
        setMovieInfos(data.content)
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

        //setSearchGroupsQuery("cv")
        setGroupInfos([])
        setMovieInfos([])
        setLikedMovies([]); // Clear the current liked movies
        setShowCreateGroupForm(false); // Hide the form

        if (!user) {
            console.error("User is not authenticated");
            return;
        }

        const idToken = await user.getIdToken(true);

        let data: any

        if (typeof event === 'string') {
            console.log("type string")
            const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/group/search-groups-get-last?query=${event}`, {
                method: 'GET',
                headers: {
                    'Authorization': 'Bearer ' + idToken,
                    'Content-Type': 'application/json'
                }
            });
            if (!response.ok) {
                console.error("Server response:", response.status, response.statusText);
                return;
            }
            data = await response.json();
            // @ts-ignore
            setGroupInfos([data])
        } else {
            const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/group/search-groups?query=${searchGroupsQuery}`, {
                method: 'GET',
                headers: {
                    'Authorization': 'Bearer ' + idToken,
                    'Content-Type': 'application/json'
                }
            });
            if (!response.ok) {
                console.error("Server response:", response.status, response.statusText);
                return;
            }
            data = await response.json();
            setGroupInfos(data.content)
        }
        // @ts-ignore
        setGroupAvatarFile('')
    };

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
        const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/avatar/user/get-signed-url`, {
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + idToken
            }
        });

        const res = await response.json()
        console.log("su", res.signedUrl)
        setSignedURL(res.signedUrl)
    }

    let attempts1 = 0
    const handleRefreshGroupAvatarSignedURL = async (groupInfoId: Number) => {
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
        const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/avatar/group/${groupInfoId}/get-signed-url`, {
            method: 'GET'
        });

        const res = await response.json()
        console.log("su", res.signedUrl)
        setGroupSignedURL(res.signedUrl)
    }

    async function handleCreateGroup(values: CreateGroupFormValues) {
        // Create the group without the avatar
        console.log(values, "vals")
        values.group_avatar = ""
        let groupId = -1
        let resj : any
        try {
            if (!user) {
                console.error("User is not authenticated");
                return;
            }
            const idToken = await user.getIdToken(true);
            const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/group/create-group`, {
                method: 'POST',
                headers: {
                    'Authorization': 'Bearer ' + idToken,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(values),
            });
            const res = await response.json()
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
                //await uploadAvatar(values.avatar, groupId);
                const signedUrl = await getGroupAvatarPutSignedUrl(groupId)
                const response = await fetch(signedUrl, {
                    method: 'PUT',
                    body: groupAvatarFile,
                    headers: {
                        'Content-Type': 'image/jpeg' // Important: the content type should match the one you specified when generating the signed URL
                    }
                });

                if (!response.ok) {
                    throw new Error(`Upload failed: ${response.status}`);
                }
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

if (!loading && user) {
        // @ts-ignore
    // @ts-ignore
    // @ts-ignore
    return (
            <div style={{ display: 'flex'}}>
                <div style={{ marginRight: '20px' }}>
                    <h1>
                        {user.email}
                    </h1>
                    <div>
                        {signedURL.length > 0 ? (
                            <>
                                <ImageComponent user={user} src={signedURL} alt={"Group Avatar"}/>
                            </>
                        ) : (
                            <></>
                        )}
                    </div>
                    <form onSubmit={handleSearchSubmit}>
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
                            <button onClick={handleCreateGroupClick}>
                                Create Group
                            </button>
                        </div>
                        <div>
                            <h2>
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
                        <div>
                            <button onClick={handleLikedMoviesClick}>
                                Liked Movies
                            </button>
                        </div>
                        <div>
                            <button onClick={handleGetGroupsClick}>
                                Groups
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
                                    <h1><u>Liked Movies</u></h1>
                                    <SearchResultsList movies={likedMovies}/>
                                </>
                            ) : movieInfos.length > 0 ? (
                                <>
                                    <h1><u>{movieInfos.length > 0 ? 'Search Results' : ''}</u></h1>
                                    <SearchResultsList movies={movieInfos}/>
                                </>
                            ) : groupInfos.length >0 ? (
                                <>
                                    <h1><u>Liked Movies</u></h1>
                                    <GroupList groups={groupInfos}/>
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
