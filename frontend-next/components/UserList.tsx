import User from "@/components/User";


interface UserListProps {
    users: User[];
    isFriend: boolean;
    isFriendRequest: boolean;
}

const UserList = ({ users, isFriend, isFriendRequest}: UserListProps) => {
    return (
        <div>
            {users.map((user: User) => (
                <User key={user.id} user={user} isFriend={isFriend} isFriendRequest={isFriendRequest} />
            ))}
        </div>
    );
}


export default UserList
