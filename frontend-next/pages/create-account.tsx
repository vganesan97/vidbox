import { useRouter } from 'next/router';
import { useState, useEffect } from 'react';
import styles from 'styles/CreateAccount.module.css'
import { useCreateUserWithEmailAndPassword } from 'react-firebase-hooks/auth';
import { Formik, Form, Field, ErrorMessage } from 'formik';

import {auth} from "@/firebase_creds";


const ErrorModal = () => {
    return (
        <div className={styles.errorModal}>
            <h2>Error!</h2>
            <p>There was an error with your registration.</p>
        </div>
    );
}

export default function CreateAccount() {
    const router = useRouter();
    const [username, setUsername] = useState(null);
    const [password, setPassword] = useState('');
    const [errorModalOpen, setErrorModalOpen] = useState(false);

    const [
        createUserWithEmailAndPassword,
        user,
        loading,
        error,
    ] = useCreateUserWithEmailAndPassword(auth);

    useEffect(() => {
        if (router.query.username) {
            // @ts-ignore
            setUsername(router.query.username);
        }
    }, [router.query.username]);

    useEffect(() => {
        if (error) {
            setErrorModalOpen(true);
        }
    }, [error]);

    interface FormValues {
        username: string
        password: string
        firstName: string
        lastName: string
        dob: string
    }

    if (!username) {
        return <div>Loading...</div>
    }

    // @ts-ignore
    const signUpWithEmailAndPassword = async (email, password, setStatus) => {
        const userCredential = await createUserWithEmailAndPassword(email, password);
        if (error) {  // <-- error is in scope from useCreateUserWithEmailAndPassword
            //console.log('errorCode', error.code);
            //console.log('errorMsg', error.message);
            setStatus({
                'errorCode': error.code,
                'errorMsg': error.message
            })
            setErrorModalOpen(true);
            // console.log('errorCode', error.code);
            // console.log('errorMsg', error.message);
        }
        console.log("userCredential", userCredential);
        // create user on backend
        // const response = await fetch('http://127.0.0.1:8081/create-user', {
        //     method: 'POST',
        //     headers: { 'Content-Type': 'application/json' },
        //     body: JSON.stringify(data),
        // })
    };

    return (
        <main className={styles.main}>
            <div className={styles.title}>Sign Up</div>
            <Formik
                initialValues={{ username: '', password: '', firstName: '', lastName: '', dob: '' }}
                validateOnChange={false} // Only validate the form when the submit button is clicked
                validateOnBlur={false}
                validate={values => {
                    const errors: Partial<FormValues> = {};
                    if (!values.username) {
                        errors.username = 'Required';
                    }
                    if (!values.password) {
                        errors.password = 'Required';
                    }
                    if (!values.firstName) {
                        errors.firstName = 'Required';
                    }
                    if (!values.lastName) {
                        errors.lastName = 'Required';
                    }
                    if (!values.dob) {
                        errors.dob = 'Required';
                    }
                    return errors;
                }}
                onSubmit={async (values, { setSubmitting, setStatus }) => {
                    // create user on firebase
                    await signUpWithEmailAndPassword(values.username, values.password, setStatus)
                    setSubmitting(false)
                }}
            >
                {({ isSubmitting, status }) => (
                    <Form>
                        <div>
                            <label className={styles.label} htmlFor="username">Username:</label>
                            <Field id="username" name="username" />
                            <ErrorMessage name="username" component="div" className={styles.error}  />
                        </div>

                        <div>
                            <label className={styles.label} htmlFor="password">Password:</label>
                            <Field id="password" name="password" type="password" />
                            <ErrorMessage name="password" component="div" className={styles.error}/>
                        </div>

                        <div>
                            <label className={styles.label} htmlFor="firstName">First Name:</label>
                            <Field id="firstName" name="firstName" />
                            <ErrorMessage name="firstName" component="div" className={styles.error}/>
                        </div>

                        <div>
                            <label className={styles.label} htmlFor="lastName">Last Name:</label>
                            <Field id="lastName" name="lastName" />
                            <ErrorMessage name="lastName" component="div" className={styles.error}/>
                        </div>

                        <div>
                            <label className={styles.label} htmlFor="dob">Date of Birth:</label>
                            <Field id="dob" name="dob" type="date" />
                            <ErrorMessage name="dob" component="div" className={styles.error}/>

                            <button type="submit" disabled={isSubmitting}>
                                Submit
                            </button>
                        </div>

                        <div style={{width: '100%'}}>
                            {errorModalOpen && <ErrorModal/>}
                        </div>
                    </Form>
                )}
            </Formik>

            {/*<form onSubmit={handleSubmitCreateAcct}>*/}
            {/*    <div>*/}
            {/*        <label htmlFor="username">Username:</label>*/}
            {/*        <input type="text" id="username" name="username" autoComplete="current-password" />*/}
            {/*    </div>*/}
            {/*    <div>*/}
            {/*        <label htmlFor="password">Password:</label>*/}
            {/*        <input type="password" id="password" name="password" autoComplete="current-password" />*/}
            {/*    </div>*/}

            {/*    <button type="submit">Submit</button>*/}
            {/*</form>*/}
        </main>
    );
}