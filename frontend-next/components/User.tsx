import { useState } from 'react';
import {auth} from "@/firebase_creds";
import { useAuthState } from 'react-firebase-hooks/auth';
import ImageComponent from "@/components/ImageComponent";
import {acceptFriendRequest, sendFriendRequest} from "@/requests/backendRequests";

type User = {
    id: number;
    username: string;
    firstName: string;
    lastName: string;
    dob: string;
    profilePic: string;
}

interface UserProps {
    user: User;
    isFriend: boolean;
    isFriendRequest: boolean;
}

const User = ({ user, isFriend, isFriendRequest }: UserProps) =>  {
    const [signedInUser, loading, error] = useAuthState(auth)
    if (user.profilePic !== null) {
        try {
            new URL(user.profilePic)
            console.log(user.profilePic)
        } catch (e) {
            console.log("error")
            console.log(e);
        }
    }

    const friendRequestClick = async () => {
        await sendFriendRequest(signedInUser, user.id)
    }

    const acceptFriendRequestClick = async () => {
        await acceptFriendRequest(signedInUser, user.id)
    }

    return (
        <div style={{
            width: '300%',
            marginLeft: '2%',
            borderBottom: '1px solid #ccc', // Add a 1px solid light grey line
            paddingBottom: '15px', // Add some padding to space out the line from the content
            marginBottom: '15px',
        }}>
            <div style={{ paddingLeft:'5px', display: 'flex', alignItems: 'center', marginBottom: '10px' }}>
                <ImageComponent user={user} src={user.profilePic !== null ? user.profilePic : "https://cdn.britannica.com/39/7139-050-A88818BB/Himalayan-chocolate-point.jpg"} alt={"Profile Avatar"}/>
                <div style={{ marginLeft: '10px' }}>
                    <h1>{user.firstName}</h1>
                    <h1>{user.lastName}</h1>
                </div>
            </div>
            <h1>Username: {user.username}</h1>
            <h1>Birthday: {user.dob}</h1>
            <div style={{display: 'flex', alignItems: 'center', }}>
                {!isFriend && <button onClick={friendRequestClick}>Send Friend Request</button>}
            </div>
            <div style={{display: 'flex', alignItems: 'center', }}>
                {isFriendRequest && <button onClick={acceptFriendRequestClick}>Accept Friend Request</button>}
            </div>
        </div>
    );
}

export default User
