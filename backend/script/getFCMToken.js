const { GoogleAuth } = require('google-auth-library');
const path = require('path');

async function getAccessToken() {
    const auth = new GoogleAuth({
        keyFile: path.join(__dirname, '../firebase-service-account.json'),
        scopes: ['https://www.googleapis.com/auth/firebase.messaging'],
    });

    const client = await auth.getClient();
    const accessTokenResponse = await client.getAccessToken();

    return accessTokenResponse.token;
}

// ทดสอบเรียก
getAccessToken()
    .then(token => {
        console.log('ACCESS TOKEN:\n', token);
    })
    .catch(err => {
        console.error('ERROR:', err);
    });
