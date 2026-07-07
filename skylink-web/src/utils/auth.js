const TOKEN_KEY = "skylink_token";
const USER_KEY = "skylink_user";

export const saveAuth = (token, user) => {
  localStorage.setItem(
    TOKEN_KEY,
    token
  );

  localStorage.setItem(
    USER_KEY,
    JSON.stringify(user)
  );
};

export const getToken = () => {
  return localStorage.getItem(
    TOKEN_KEY
  );
};

export const getUser = () => {
  const storedUser =
    localStorage.getItem(USER_KEY);

  if (!storedUser) {
    return null;
  }

  try {
    return JSON.parse(storedUser);
  } catch (error) {
    console.error(
      "Unable to parse stored SkyLink user.",
      error
    );

    return null;
  }
};

export const isAuthenticated = () => {
  return Boolean(getToken());
};

export const clearAuth = () => {
  localStorage.removeItem(
    TOKEN_KEY
  );

  localStorage.removeItem(
    USER_KEY
  );
};

export const logout = () => {
  clearAuth();
};