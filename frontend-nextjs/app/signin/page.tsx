import { SignInForm } from '@/features/authentication/signin';

export default function Signin() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-between p-24">
      <SignInForm />
    </main>
  );
}
