import { useState} from 'react';
import {auth} from "@/firebase_creds";
import { useAuthState } from 'react-firebase-hooks/auth';
import ImageComponent from "@/components/ImageComponent";

type Group = {
    id: number;
    groupAvatar: string;
    groupDescription: string;
    privacy: string;
    groupName: string;
    isMember: boolean;
}

interface GroupProps {
    group: Group;
}

const Group = ({ group }: GroupProps) =>  {
    const [groupSignedURL, setGroupSignedURL] = useState<string>('');
    const [user, loading, error] = useAuthState(auth)
    let attempts = 0
    const handleRefreshGroupAvatarSignedURL = async (groupInfoId: Number) => {
        console.log("refresh")
        if (attempts >= 3) {  // only try to refresh the URL up to 3 times
            console.error('Failed to load image after 3 attempts');
            attempts = 0
            return;
        }

        if (!user) {
            console.error("User is not authenticated");
            return;
        }
        const response = await fetch(`https://vidbox-7d2c1.uc.r.appspot.com/avatar/group/${groupInfoId}/get-signed-url`, {
            method: 'GET'
        });

        const res = await response.json()
        console.log("su", res.signedUrl)
        setGroupSignedURL(res.signedUrl)
    }

    const joinGroup = async () => {
        if (!user) {
            console.error("User is not authenticated");
            return;
        }
        const idToken = await user.getIdToken(true);
        const response = await fetch(`http://127.0.0.1:8081/join-group/${group.id}`, {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + idToken,
                'Content-Type': 'application/json'
            }
        });
        const res = await response.json()
        console.log("joined group ", res)
    }

    return (
        <div>
            <h1>Group Name: {group.groupName}</h1>
            <h2>Group Description: {group.groupDescription}</h2>
            <h2>Privacy: {group.privacy}</h2>
            <h2>Member: {group.isMember ? 'true' : 'false'}</h2>
            <div style={{display: 'flex', alignItems: 'center'}}>
                <ImageComponent user={{}} src={group.groupAvatar} alt={"Group Avatar"}/>
                {/*<img*/}
                {/*    src={group.groupAvatar}*/}
                {/*    onError={() => handleRefreshGroupAvatarSignedURL(group.id)}*/}
                {/*    alt="Group Avatar"*/}
                {/*    style={{width: "100px", height: "100px"}}*/}
                {/*/>*/}
                {!group.isMember && <button onClick={joinGroup}>Join Group</button>}
            </div>
        </div>
    );
}

export default Group
