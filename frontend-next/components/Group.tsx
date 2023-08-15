import { useState} from 'react';
import {auth} from "@/firebase_creds";
import { useAuthState } from 'react-firebase-hooks/auth';
import ImageComponent from "@/components/ImageComponent";
import {joinGroupRequest} from "@/requests/backendRequests";

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

    const joinGroup = async () => {
        await joinGroupRequest(user, group.id)
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
