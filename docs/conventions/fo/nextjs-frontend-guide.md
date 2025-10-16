# Next.js Frontend 개발 컨벤션

Next.js 14.2.x, React 18.3.x

---

## 1. 코딩 규칙

- 컴포넌트는 처음부터 분리 (한 파일에 수백 줄 금지)
- `||` 대신 `??` 사용 (null/undefined만 체크)
- `.map()` 사용 시 `key` 필수
- 비동기 함수는 `try-catch` 필수
- 조건부 렌더링: 삼항 연산자 또는 `&&`
- Props와 객체는 구조 분해 할당
- Optional Chaining `?.` 적극 사용

---

## 2. 필수 패키지

### Zod (스키마 검증)
```bash
pnpm add zod
```
```typescript
import { z } from 'zod'

const schema = z.object({
  name: z.string(),
  price: z.number().positive(),
})

type Product = z.infer<typeof schema>

const result = schema.safeParse(data)
if (!result.success) {
  console.error(result.error)
  return
}
```

### tailwind-merge + clsx
```bash
pnpm add tailwind-merge clsx
```

`src/lib/utils.ts`:
```typescript
import { type ClassValue, clsx } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}
```

---

## 3. 에러 핸들링

### Server Action
```typescript
export async function createItem(formData: FormData) {
  try {
    await sql`INSERT INTO ...`
  } catch (error) {
    return { message: 'Database Error' }
  }
  revalidatePath('/items')
  redirect('/items')
}
```

### error.tsx
```typescript
'use client'

export default function Error({ error, reset }) {
  return (
    <div>
      <h2>Something went wrong!</h2>
      <button onClick={() => reset()}>Try again</button>
    </div>
  )
}
```

### not-found.tsx
```typescript
import { notFound } from 'next/navigation'

// 페이지에서
if (!data) notFound()

// not-found.tsx
export default function NotFound() {
  return <div><h2>404 Not Found</h2></div>
}
```

---

## 4. 데이터 페칭

### Server Component
```typescript
async function getData() {
  const res = await fetch('https://api.example.com/data', {
    cache: 'no-store', // 또는 next: { revalidate: 3600 }
  })
  if (!res.ok) throw new Error('Failed to fetch')
  return res.json()
}

export default async function Page() {
  const data = await getData()
  return <div>{data.title}</div>
}
```

### Client Component
```typescript
'use client'
import { useState, useEffect } from 'react'

export default function Page() {
  const [data, setData] = useState(null)

  useEffect(() => {
    fetch('/api/data')
      .then(res => res.json())
      .then(setData)
  }, [])

  return <div>{data?.title}</div>
}
```

### API Routes
```typescript
// app/api/posts/route.ts
import { NextResponse } from 'next/server'

export async function GET() {
  const data = await fetch('https://api.example.com/posts')
  return NextResponse.json(await data.json())
}

export async function POST(request: Request) {
  const body = await request.json()
  return NextResponse.json({ success: true })
}
```

### Server Actions
```typescript
'use server'
import { revalidatePath } from 'next/cache'

export async function createPost(formData: FormData) {
  try {
    await fetch('https://api.example.com/posts', {
      method: 'POST',
      body: JSON.stringify({ title: formData.get('title') })
    })
    revalidatePath('/posts')
    return { success: true }
  } catch (error) {
    return { success: false, error: error.message }
  }
}

// 사용: <form action={createPost}>
```

---

## 5. 상태 관리 (Zustand)

```bash
pnpm add zustand
```

```typescript
import { create } from 'zustand'

const useStore = create((set) => ({
  count: 0,
  increase: () => set((state) => ({ count: state.count + 1 })),
  reset: () => set({ count: 0 }),
}))

// 사용
const count = useStore((state) => state.count)
const increase = useStore((state) => state.increase)
```

---

## 6. 파일 구조

```
src/
├── components/
│   ├── ui/          # Button, Input, Modal
│   ├── layout/      # Header, Footer, Sidebar
│   └── features/    # auth/, products/, cart/
├── lib/
│   └── utils.ts
├── app/
│   ├── api/
│   ├── (routes)/
│   └── actions.ts
```

**네이밍:**
- 컴포넌트: PascalCase
- 유틸/함수: camelCase
- 상수: UPPER_SNAKE_CASE

---

## 7. 성능 최적화

### Image
```typescript
import Image from 'next/image'

<Image src="/profile.jpg" alt="Profile" width={500} height={500} priority />
```

### Dynamic Import
```typescript
import dynamic from 'next/dynamic'

const Heavy = dynamic(() => import('@/components/Heavy'), {
  loading: () => <div>Loading...</div>,
  ssr: false
})
```

### 메모이제이션
```typescript
import { memo, useCallback, useMemo } from 'react'

const Component = memo(({ data }) => <div>{data}</div>)
const handleClick = useCallback(() => {}, [])
const total = useMemo(() => items.reduce((sum, item) => sum + item.price, 0), [items])
```

---

## 8. TypeScript

### Props
```typescript
interface ButtonProps {
  children: React.ReactNode
  onClick?: () => void
  variant?: 'primary' | 'secondary'
}

export default function Button({ children, onClick, variant = 'primary' }: ButtonProps) {
  return <button onClick={onClick}>{children}</button>
}
```

### API 타입
```typescript
// 성공 응답
interface ApiResponse<T> {
  code: string
  message: string
  data: T
}

// 에러 응답
interface ErrorResponse {
  timestamp: string
  code: string
  message: string
}

// 사용 예시
interface Payment {
  paymentId: number
  orderId: string
  amount: number
  status: string
}

async function getPayment(id: number): Promise<ApiResponse<Payment>> {
  const res = await fetch(`/api/payments/${id}`)
  const data = await res.json()
  
  if (data.code !== '0000') {
    throw new Error(data.message)
  }
  
  return data
}
```

### any 금지
- `any` 대신 `unknown` 또는 구체적 타입
- 타입 가드로 타입 좁히기