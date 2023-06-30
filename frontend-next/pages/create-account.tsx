import { useRouter } from 'next/router';
import { useState, useEffect } from 'react';
import styles from "@/styles/Home.module.css";
import { getAuth, createUserWithEmailAndPassword } from "firebase/auth";
import {auth} from "@/firebase_creds";

export default function CreateAccount() {
    const router = useRouter();
    const [username, setUsername] = useState(null);

    useEffect(() => {
        if (router.query.username) {
            // @ts-ignore
            setUsername(router.query.username);
        }
    }, [router.query.username]);

    if (!username) {
        return <div>Loading...</div>
    }

    // @ts-ignore
    const signUpWithEmailAndPassword = async (auth, email, password) => {
        try {
            const userCredential = await createUserWithEmailAndPassword(auth, email, password);
            const user = userCredential.user;
            // Continue processing the user as needed
            return user;
        } catch (error) {
            // @ts-ignore
            const errorCode = error.code;
            // @ts-ignore
            const errorMessage = error.message;
            // Handle the error as needed
            throw error;
        }
    };

    // @ts-ignore
    const handleSubmitCreateAcct = async (e) => {
        e.preventDefault();
        const formData = new FormData(e.target);
        const data = {
            username: formData.get('username'),
            password: formData.get('password'),
        };

        const x =  await signUpWithEmailAndPassword(auth, data.username, data.password)
        console.log(x)

        const response = await fetch('http://127.0.0.1:8081/create_account', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data),
        });
    }

    return (
        <main className={styles.main}>
            <form onSubmit={handleSubmitCreateAcct}>
                <div>
                    <label htmlFor="username">Username:</label>
                    <input type="text" id="username" name="username" autoComplete="current-password" />
                </div>
                <div>
                    <label htmlFor="password">Password:</label>
                    <input type="password" id="password" name="password" autoComplete="current-password" />
                </div>

                <button type="submit">Submit</button>
            </form>
        </main>
    );
}