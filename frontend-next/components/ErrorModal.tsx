import styles from 'styles/CreateAccount.module.css'

// @ts-ignore
const ErrorModal = ({error}) => {
    return (
        <div className={styles.errorModal}>
            <h2>Error!</h2>
            <p><b>Code: </b>{error.code}</p>
            <p><b>Message: </b>{error.msg}</p>
        </div>
    );
}

export default ErrorModal