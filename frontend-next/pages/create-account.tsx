import {useRouter} from 'next/router';
import {useEffect, useState} from 'react';
import styles from 'styles/CreateAccount.module.css'
import {useCreateUserWithEmailAndPassword} from 'react-firebase-hooks/auth';
import {ErrorMessage, Field, Form, Formik} from 'formik';
import {auth} from "@/firebase_creds";
import ErrorModal from '../components/ErrorModal';


export default function CreateAccount() {
    const router = useRouter();
    const [errorMsg, setErrorMsg] = useState({code: '', msg: ''})
    const [errorModalOpen, setErrorModalOpen] = useState(false);

    const [
        createUserWithEmailAndPassword,
        user,
        loading,
        error
    ] = useCreateUserWithEmailAndPassword(auth);

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

    interface FormValues {
        username: string
        password: string
        firstName: string
        lastName: string
        dob: string
    }

    // Handle the error in signUpWithEmailAndPassword function
    // @ts-ignore
    const signUpWithEmailAndPassword = async (values) => {
        try {
            const userCredential = await createUserWithEmailAndPassword(values.username, values.password);

            // Handle if user creation is successful
            if (userCredential) {
                values.idToken = await userCredential.user.getIdToken(true);
                const response = await fetch('https://vidbox-7d2c1.uc.r.appspot.com/create-user', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(values),
                });

                setErrorMsg({
                    code: '',
                    msg: ''
                });

                router.push({
                    pathname: '/dashboard',
                    query: { username: values.username },
                });
            }

        } catch (error) {
            // This error will be handled below
            console.error("An error occurred:", error);
        }

        // If Firebase returned an error, handle it here
        if (error) {
            setErrorModalOpen(true);
            setErrorMsg({
                code: error.code,
                msg: error.message,
            });
        }
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
                onSubmit={
                    async (values, { setSubmitting }) => {
                        await signUpWithEmailAndPassword(values)
                        setSubmitting(false)
                }}
            >
                {({ isSubmitting}) => (
                    <Form>
                        <div>
                            <label className={styles.label} htmlFor="username">Username</label>
                            <Field id="username" name="username" />
                            <ErrorMessage name="username" component="div" className={styles.error}  />
                        </div>

                        <div>
                            <label className={styles.label} htmlFor="password">Password</label>
                            <Field id="password" name="password" type="password" />
                            <ErrorMessage name="password" component="div" className={styles.error}/>
                        </div>

                        <div>
                            <label className={styles.label} htmlFor="firstName">First Name</label>
                            <Field id="firstName" name="firstName" />
                            <ErrorMessage name="firstName" component="div" className={styles.error}/>
                        </div>

                        <div>
                            <label className={styles.label} htmlFor="lastName">Last Name</label>
                            <Field id="lastName" name="lastName" />
                            <ErrorMessage name="lastName" component="div" className={styles.error}/>
                        </div>

                        <div>
                            <label className={styles.label} htmlFor="dob">Date of Birth</label>
                            <Field id="dob" name="dob" type="date" />
                            <ErrorMessage name="dob" component="div" className={styles.error}/>

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
        </main>
    );
}