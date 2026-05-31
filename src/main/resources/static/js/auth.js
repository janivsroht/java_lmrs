// LMRS Auth Module - JWT storage, token refresh, authenticated fetch wrapper

const Auth = {
    TOKEN_KEY: 'lmrs_access_token',
    REFRESH_KEY: 'lmrs_refresh_token',
    USER_KEY: 'lmrs_user',

    getToken() {
        return localStorage.getItem(this.TOKEN_KEY);
    },

    getRefreshToken() {
        return localStorage.getItem(this.REFRESH_KEY);
    },

    setTokens(accessToken, refreshToken) {
        localStorage.setItem(this.TOKEN_KEY, accessToken);
        localStorage.setItem(this.REFRESH_KEY, refreshToken);
    },

    getUser() {
        const data = localStorage.getItem(this.USER_KEY);
        return data ? JSON.parse(data) : null;
    },

    setUser(user) {
        localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    },

    isAuthenticated() {
        return !!this.getToken();
    },

    clear() {
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.REFRESH_KEY);
        localStorage.removeItem(this.USER_KEY);
    },

    logout() {
        this.clear();
        window.location.href = '/login';
    },

    async refreshAccessToken() {
        const refreshToken = this.getRefreshToken();
        if (!refreshToken) return null;

        try {
            const resp = await fetch('/api/v1/auth/refresh', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ refreshToken })
            });
            if (!resp.ok) return null;
            const data = await resp.json();
            this.setTokens(data.accessToken, data.refreshToken);
            return data.accessToken;
        } catch {
            return null;
        }
    },

    async authFetch(url, options = {}) {
        const token = this.getToken();
        if (!token) {
            this.logout();
            throw new Error('No token');
        }

        if (!options.headers) options.headers = {};
        options.headers['Authorization'] = 'Bearer ' + token;
        if (!options.headers['Content-Type'] && options.body) {
            options.headers['Content-Type'] = 'application/json';
        }

        let resp = await fetch(url, options);

        if (resp.status === 401) {
            const newToken = await this.refreshAccessToken();
            if (newToken) {
                options.headers['Authorization'] = 'Bearer ' + newToken;
                resp = await fetch(url, options);
            } else {
                this.logout();
                throw new Error('Session expired');
            }
        }

        return resp;
    },

    requireAuth() {
        if (!this.isAuthenticated()) {
            window.location.href = '/login';
            return false;
        }
        return true;
    }
};
