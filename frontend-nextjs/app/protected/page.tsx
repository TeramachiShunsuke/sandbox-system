import { SignOutButton } from '@/features/authentication/signout';

const ProtectedHome = () => {
  return (
    <main>
      <div>
        <h1>Welcome to your app</h1>
        <p>
          Get started by editing <code>app/page.tsx</code>
        </p>
        <SignOutButton />
      </div>
    </main>
  );
};

export default ProtectedHome;
