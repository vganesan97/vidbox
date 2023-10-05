import axios from 'axios';

// @ts-ignore
const vidboxApiClient = async (user: any) => {
    if (!user) throw new Error('User must be provided');
    const idToken = await user.getIdToken(true);
    return axios.create({
        baseURL: process.env.NEXT_PUBLIC_API_BASE_URL,
        headers: {
            'Authorization': 'Bearer ' + idToken,
            'Content-Type': 'application/json'
        }
        // Additional configuration here
    });
};

export default vidboxApiClient;

