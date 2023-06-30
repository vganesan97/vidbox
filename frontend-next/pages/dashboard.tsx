import { useRouter } from 'next/router';
import { useState, useEffect } from 'react';
import { useAuthState } from 'react-firebase-hooks/auth';
import {auth} from "@/firebase_creds";


export default function Dashboard() {
  const router = useRouter();
  const [username, setUsername] = useState(null);
  const [user, loading, error] = useAuthState(auth)

  useEffect(() => {
    if (router.query.username) {
      // @ts-ignore
      setUsername(router.query.username);
    }
    if (user) {
      user.getIdToken(true)
          .then((idToken) => {
              console.log("idToken:", idToken)
              // Send the idToken to your backend server
              // ...
          })
          .catch((error) => {
              // Handle error
              console.error(error);
          });
    }
  }, [router.query.username, user]);

  if (!loading && user) {
    return (
        <div>
          <h1>
              Welcome,
              {user.email}:
              {user.displayName},
              {user.metadata.creationTime}
          </h1>
        </div>
    );
  } else {
    return (
        <div>
          <h1>loading..</h1>
        </div>
    )
  }

}