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
        <div style={{
            width: '300%',
            marginLeft: '2%',
            borderBottom: '1px solid #ccc', // Add a 1px solid light grey line
            paddingBottom: '15px', // Add some padding to space out the line from the content
            marginBottom: '15px',
        }}>
            <h1>Group Name: {group.groupName}</h1>
            <h1>Group Description: {group.groupDescription}</h1>
            <h1>Privacy: {group.privacy}</h1>
            <h1>Member: {group.isMember ? 'true' : 'false'}</h1>
            <div style={{display: 'flex', alignItems: 'center', }}>
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
