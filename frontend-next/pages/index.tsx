import { Formik, Field, Form, ErrorMessage } from 'formik';
import { useRouter } from 'next/router';
import { useState, useEffect } from 'react';
import styles from 'styles/CreateAccount.module.css'
import { useSignInWithEmailAndPassword } from 'react-firebase-hooks/auth';
import { auth } from "@/firebase_creds";
import ErrorModal from "@/components/ErrorModal";
import {
    fetchGoogleUserDOB,
    refreshProfileAvatarSignedURLRequest,
    signInRequest,
    signUpUserRequest
} from "@/requests/backendRequests";
import { getAuth, signInWithPopup, GoogleAuthProvider, getAdditionalUserInfo, signInWithRedirect, getRedirectResult } from "firebase/auth";


interface SignInFormValues {
    username: string;
    password: string;
    firstName?: string;
    lastName?: string;
}

export default function Home() {
    const router = useRouter();
    const [errorModalOpen, setErrorModalOpen] = useState(false);
    const [errorMsg, setErrorMsg] = useState({code: '', msg: ''})
    const [routingToSignUp, setRoutingToSignUp] = useState(false);  // Add this line
    const [
        signInWithEmailAndPassword,
        user,
        loading,
        error,
    ] = useSignInWithEmailAndPassword(auth);

    useEffect(() => {
        // @ts-ignore
        let errorTimeout;
        if (error) {
            setErrorModalOpen(true);
            setErrorMsg({
                code: error.code,
                msg: error.message
            });

            errorTimeout = setTimeout(() => {
                setErrorModalOpen(false);
                setErrorMsg({
                    code: '',
                    msg: ''
                });
            }, 5000);  // Clear the error message after 5 seconds
        }
        // @ts-ignore
        return () => clearTimeout(errorTimeout);  // Clean up on unmount
    }, [error]);

    useEffect(() => {
        // Check for redirect result after mounting the component
        const auth = getAuth();
        getRedirectResult(auth)
            .then(async (result) => {
                // Handle the results here
                if (result.user) {
                    const user = result.user;
                    const credential = GoogleAuthProvider.credentialFromResult(result);
                    const token = credential?.accessToken;
                    const x = getAdditionalUserInfo(result)
                    const dob = await fetchGoogleUserDOB(token as string);
                    console.log(x, user, credential, token)

                    // Place your existing logic for new or existing users here
                    // You can also use router.push here as needed

                    if (x.isNewUser) {
                        let res = await signUpUserRequest(user, {
                            username: user.email,
                            firstName: x.profile ? x.profile.given_name : "no first name",
                            lastName: x.profile ? x.profile.family_name : "no first name",
                            dob: dob,
                            idToken: await user.getIdToken(true)
                        })
                        router.push({
                            pathname: '/dashboard',
                            query: {
                                username: res.username,
                                firstName: res.firstName,
                                lastName: res.lastName,
                                uid: res.uid
                            },
                        });
                    } else {
                        const res = await signInRequest(user)
                        const res1 = await refreshProfileAvatarSignedURLRequest(user)
                        router.push({
                            pathname: '/dashboard',
                            query: {
                                username: res.username,
                                firstName: res.firstName,
                                lastName: res.lastName,
                                uid: res.uid,
                                signedURL: res1.signedUrl
                            }
                        })
                    }
                }
            })
            .catch((error) => {
                // Handle errors here
                const errorCode = error.code;
                const errorMessage = error.message;
                console.log(`Error code: ${errorCode}, Error message: ${errorMessage}`);
            });
    }, []);

    const signIn = async (values: SignInFormValues) => {
        try {
            const userCredential = await signInWithEmailAndPassword(values.username, values.password);
            if (userCredential == null) return
            const res = await signInRequest(userCredential.user)
            const res1 = await refreshProfileAvatarSignedURLRequest(userCredential.user)
            router.push({
                pathname: '/dashboard',
                query: {
                    username: res.username,
                    firstName: res.firstName,
                    lastName: res.lastName,
                    uid: res.uid,
                    signedURL: res1.signedUrl
                }
            })
            setErrorMsg({
                code: '',
                msg: ''
            });
        } catch (error) {
            console.error('An error occurred:', error);
        }
        if (error) {
            setErrorModalOpen(true);
            setErrorMsg({
                code: error.code,
                msg: error.message,
            });
        }
    };


    const route = () => {
        setRoutingToSignUp(true);  // Add this line
        router.push('/create-account');
    }

    const handleGoogleSignIn = async () => {
        const provider = new GoogleAuthProvider()
        provider.addScope('https://www.googleapis.com/auth/user.birthday.read')
        provider.addScope('https://www.googleapis.com/auth/userinfo.profile')

        const auth = getAuth()
        signInWithRedirect(auth, provider)
            .then(async (result) => {
                // This gives you a Google Access Token. You can use it to access the Google API.
                const credential = GoogleAuthProvider.credentialFromResult(result);
                if (credential == null) return

                const token = credential.accessToken;
                const user = result.user
                console.log(user)

                const dob = await fetchGoogleUserDOB(token as string);
                const x = getAdditionalUserInfo(result)

                if (x == null) {
                    console.log("not able to find additional user info")
                    return
                }

                console.log(dob)
                console.log(x)

                if (x.isNewUser) {
                    let res = await signUpUserRequest(user, {
                        username: user.email,
                        firstName: x.profile ? x.profile.given_name : "no first name",
                        lastName: x.profile ? x.profile.family_name : "no first name",
                        dob: dob,
                        idToken: await user.getIdToken(true)
                    })
                    router.push({
                        pathname: '/dashboard',
                        query: {
                            username: res.username,
                            firstName: res.firstName,
                            lastName: res.lastName,
                            uid: res.uid
                        },
                    });
                } else {
                    const res = await signInRequest(user)
                    const res1 = await refreshProfileAvatarSignedURLRequest(user)
                    router.push({
                        pathname: '/dashboard',
                        query: {
                            username: res.username,
                            firstName: res.firstName,
                            lastName: res.lastName,
                            uid: res.uid,
                            signedURL: res1.signedUrl
                        }
                    })
                }
            }).catch((error) => {
            // Handle Errors here.
            const errorCode = error.code;
            const errorMessage = error.message;
            // The email of the user's account used.
            const email = error.customData.email;
            // The AuthCredential type that was used.
            const credential = GoogleAuthProvider.credentialFromError(error);
            // ...
        });

    }

  return (
      <main className={styles.main}>
          <div className={styles.title}>Sign In</div>
          <Formik
              initialValues={{ username: '', password: '' }}
              validateOnChange={false}
              validateOnBlur={false}
              validate={(values) => {
                  const errors: Partial<SignInFormValues> = {};
                  if (!values.username) errors.username = 'Required';
                  if (!values.password) errors.password = 'Required';

                return errors;
            }}
            onSubmit={
                async (values, { setSubmitting }) => {
                    await signIn(values);
                    setSubmitting(false);
                }
            }
          >
            {({ isSubmitting }) => (
              <Form>
                  <div>
                      <label className={styles.label} htmlFor="username">Username</label>
                      <Field id="username" name="username" />
                      <ErrorMessage name="username" component="div" className={styles.error} />
                  </div>

                  <div>
                      <label className={styles.label} htmlFor="password">Password</label>
                      <Field id="password" name="password" type="password" />
                      <ErrorMessage name="password" component="div" className={styles.error} />

                      <button type="submit" disabled={isSubmitting}>
                          {loading && !routingToSignUp ? "Loading..." : "Submit"}
                      </button>
                  </div>

                  <div className={styles.signupWrapper}>
                      <button type="button" onClick={route}>
                          Sign Up
                      </button>
                  </div>

                  <button onClick={handleGoogleSignIn}>
                      Google Sign In
                  </button>

                  <div style={{width: '100%'}}>
                      {errorModalOpen && !routingToSignUp && <ErrorModal error={errorMsg}/>}
                  </div>
              </Form>
            )}
        </Formik>
      </main>
  );
}
