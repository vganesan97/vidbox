import {auth} from "@/firebase_creds";
import { useRouter } from 'next/router';
import { useSignOut } from 'react-firebase-hooks/auth';


const SignOut = () => {
    const [signOut, loading, error] = useSignOut(auth);
    const router = useRouter();

    if (error) {
        return (
            <div>
                <p>Error: {error.message}</p>
            </div>
        );
    }
    if (loading) {
        return <p>Loading...</p>;
    }
    return (
        <div className="App">
            <button
                onClick={async () => {
                    const success = await signOut();
                    if (success) {
                        router.push('/');
                    }
                }}
            >
                Sign out
            </button>
        </div>
    );
}

export default SignOut