import vidboxApiClient from "@/requests/vidboxBackendClient";
import axios from 'axios';


export const joinGroupRequest = async (user: any, groupId: number) => {
    try {
        const client = await vidboxApiClient(user);
        const response = await client.post(`/group/join-group/${groupId}`);
        console.log("joined group asdfasdf ", response.data);
    } catch (error) {
        console.error("An error occurred:", error);
    }
};

export const refreshProfileAvatarSignedURLRequest = async (user: any) => {
    try {
        const client = await vidboxApiClient(user);
        const response = await client.get('/avatar/user/get-signed-url');
        console.log("signed url ", response.data);
        return response.data
    } catch (error) {
        console.error("An error occurred:", error);
    }
}

export const likeMovieRequest = async (user: any, movieId: number) => {
    try {
        const client = await vidboxApiClient(user);
        const data = JSON.stringify({movieId: movieId})
        const response = await client.post('/movies/like-movie', data)
        return response.data
    } catch (error) {
        console.error("An error occurred:", error)
    }
}

export const createReviewRequest = async (user: any, reviewFormValues: any) => {
    try {
        const client = await vidboxApiClient(user);
        const data = JSON.stringify(reviewFormValues)
        const response = await client.post('/review/create', data)
    } catch (error) {
        console.error("An error occurred:", error)
    }
}

export const signUpUserRequest = async (user: any, signUpFormValues: any) => {
    try {
        const client = await vidboxApiClient(user);
        const data = JSON.stringify(signUpFormValues)
        const response = await client.post('/user/create-user', data)
        return response.data
    } catch (error) {
        console.error("An error occurred:", error)
    }
}

export const getProfileAvatarPutSignedUrlRequest = async (user: any) => {
    try {
        const client = await vidboxApiClient(user);
        const response = await client.get('/avatar/user/put-signed-url')
        return response.data
    } catch (error) {
        console.error("An error occurred:", error)
    }
}

export const getGroupAvatarPutSignedUrlRequest = async (user: any, groupId: number) => {
    try {
        const client = await vidboxApiClient(user);
        const response = await client.get(`/avatar/group/${groupId}/put-signed-url`)
        return response.data
    } catch (error) {
        console.error("An error occurred:", error)
    }
}

export const putProfileAvatarRequest = async (signedUrl: string, file: File) => {
    try {
        const response = await axios.put(signedUrl, file, {
            headers: {
                'Content-Type': 'image/jpeg', // Important: the content type should match the one you specified when generating the signed URL
            },
        });
        return response.data;
    } catch (error) {
        console.error("An error occurred:", error);
    }
}

export const putGroupAvatarRequest = async (signedUrl: string, file: File) => {
    try {
        const response = await axios.put(signedUrl, file, {
            headers: {
                'Content-Type': 'image/jpeg', // Important: the content type should match the one you specified when generating the signed URL
            },
        });
        return response.data;
    } catch (error) {
        console.error("An error occurred:", error);
    }
}

export const getLikedMoviesRequest = async (user: any) => {
    try {
        const client = await vidboxApiClient(user);
        const response = await client.get('/movies/liked-movies')
        return response.data
    } catch (error) {
        console.error("An error occurred:", error)
    }
}

export const getGroupsRequest = async (user: any) => {
    try {
        const client = await vidboxApiClient(user);
        const response = await client.get('/group/get-groups')
        console.log(response.data)
        return response.data
    } catch (error) {
        console.error("An error occurred:", error)
    }
}

export const searchMoviesRequest = async (user: any, searchQuery: string) => {
    try {
        const client = await vidboxApiClient(user);
        const response = await client.get(`/movies/search-movies?query=${searchQuery}`)
        return response.data
    } catch (error) {
        console.error("An error occurred:", error)
    }
}

export const searchGroupsGetLastRequest = async (user: any, event: string) => {
    try {
        const client = await vidboxApiClient(user);
        const response = await client.get(`/group/search-groups-get-last?query=${event}`)
        return response.data
    } catch (error) {
        console.error("An error occurred:", error)
    }
}

export const searchGroupsRequest = async (user: any, searchGroupsQuery: string) => {
    try {
        const client = await vidboxApiClient(user);
        const response = await client.get(`/group/search-groups?query=${searchGroupsQuery}`)
        return response.data
    } catch (error) {
        console.error("An error occurred:", error)
    }
}

export const createGroupRequest = async (user: any, groupFormValues: any) => {
    try {
        const client = await vidboxApiClient(user);
        const data = JSON.stringify(groupFormValues)
        const response = await client.post('/group/create-group', data)
        return response.data
    } catch (error) {
        console.error("An error occurred:", error)
    }
}

export const signInRequest = async (user: any, signInFormValues: any) => {
    try {
        const client = await vidboxApiClient(user);
        const data = JSON.stringify(signInFormValues)
        const response = await client.post('/user/login', data)
        return response.data
    } catch (error) {
        console.error("An error occurred:", error)
    }
}


