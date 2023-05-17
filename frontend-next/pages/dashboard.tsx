import { useRouter } from 'next/router';
import { useState, useEffect } from 'react';

export default function Dashboard() {
  const router = useRouter();
  const [username, setUsername] = useState(null);

  useEffect(() => {
    if (router.query.username) {
      setUsername(router.query.username);
    }
  }, [router.query.username]);

  if (!username) {
    return <div>Loading...</div>;
  }

  return (
    <div>
      <h1>Welcome, {username}!</h1>
    </div>
  );
}