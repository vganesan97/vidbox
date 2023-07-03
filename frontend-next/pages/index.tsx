import { Inter } from 'next/font/google';
import styles from '@/styles/Home.module.css';
import { useRouter } from 'next/router';
import { getAuth, signInWithEmailAndPassword } from "firebase/auth";

const inter = Inter({ subsets: ['latin'] });

export default function Home() {
  const router = useRouter();

  // @ts-ignore
  const signInWithEmailAndPass = async (auth, email: string, password: string): Promise<UserCredential> => {
    try {
      const userCredential = await signInWithEmailAndPassword(auth, email, password);
      const user = userCredential.user;
      // Continue processing the user as needed
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
  const handleSubmit = async (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    const data = {
      username: formData.get('username'),
      password: formData.get('password'),
    };

    const response = await fetch('http://127.0.0.1:8081/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });

    const result = await response.json();
    console.log(result)

    if (response.ok) {
      router.push({
        pathname: '/dashboard',
        query: { username: result.username },
      });
    } else {
      console.log('invalid login')
    }
  };

  const handleCreateAcct = async () => {
    router.push({
      pathname: '/create-account',
      query: { username: "result.username" },
    });
  };

  return (
      <main className={styles.main}>
        <form onSubmit={handleSubmit}>
          <div>
            <label htmlFor="username">Username:</label>
            <input type="text" id="username" name="username" autoComplete="current-password" />
          </div>
          <div>
            <label htmlFor="password">Password:</label>
            <input type="password" id="password" name="password" autoComplete="current-password" />
          </div>
          <button type="submit">Login</button>
        </form>
        <button onClick={handleCreateAcct} type="submit">Sign Up</button>
      </main>
  );
}
