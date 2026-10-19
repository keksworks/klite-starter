type FormEvent = Event & {currentTarget: HTMLInputElement}

interface Window {
  apiVersion?: string
  e2eTest?: boolean
}
