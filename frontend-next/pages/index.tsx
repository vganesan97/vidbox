import { Formik, Field, Form, ErrorMessage } from 'formik';
import { useRouter } from 'next/router';
import { useState, useEffect } from 'react';
import styles from 'styles/CreateAccount.module.css'
import { useSignInWithEmailAndPassword } from 'react-firebase-hooks/auth';
import { auth } from "@/firebase_creds";
import ErrorModal from "@/components/ErrorModal";

interface FormValues {
    username: string;
    password: string;
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

    const signIn = async (values: FormValues) => {
        try {
            const userCredential = await signInWithEmailAndPassword(values.username, values.password);
            if (userCredential == null) return
            const idToken = await userCredential.user.getIdToken(true);
            const response = await fetch('http://127.0.0.1:8081/login', {
                method: 'POST',
                headers: {
                    'Authorization': 'Bearer ' + idToken,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(values),
            });

            const res = await response.json()
            console.log("login response", res)
            router.push({
                pathname: '/dashboard',
                query: {
                    username: values.username,
                    uid: res.uid
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


  return (
      <main className={styles.main}>
          <div className={styles.title}>Sign In</div>
          <Formik
              initialValues={{ username: '', password: '' }}
              validateOnChange={false}
              validateOnBlur={false}
              validate={(values) => {
                  const errors: Partial<FormValues> = {};
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
                      <button onClick={route}>
                          Sign Up
                      </button>
                  </div>

                  <div style={{width: '100%'}}>
                      {errorModalOpen && !routingToSignUp && <ErrorModal error={errorMsg}/>}
                  </div>
              </Form>
            )}
        </Formik>
      </main>
  );
}
