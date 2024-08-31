export const decodeJWT = (token: string) => {
  // JWT は 3 つの部分に分かれているので、ピリオドで分割
  const base64Url = token.split('.')[1];
  // Base64Url エンコーディングを Base64 に変換
  const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
  // Base64 エンコードされた文字列をデコード
  const jsonPayload = decodeURIComponent(
    atob(base64)
      .split('')
      .map((c) => `%${`00${c.charCodeAt(0).toString(16)}`.slice(-2)}`)
      .join(''),
  );

  // デコードされた JSON 文字列をパースしてオブジェクトに変換
  return JSON.parse(jsonPayload);
};
