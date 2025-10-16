import React from 'react';
import { cn } from '@/lib/utils';

interface CheckboxProps extends Omit<React.InputHTMLAttributes<HTMLInputElement>, 'type'> {
  label?: string;
  error?: string;
}

const Checkbox = React.forwardRef<HTMLInputElement, CheckboxProps>(
  ({ className, label, error, id, ...props }, ref) => {
    const checkboxId = id || `checkbox-${Math.random().toString(36).substr(2, 9)}`;

    return (
      <div className="space-y-2">
        <div className="flex items-center space-x-2">
          <input
            type="checkbox"
            id={checkboxId}
            className={cn(
              "h-4 w-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500",
              error && "border-red-500",
              className
            )}
            ref={ref}
            {...props}
          />
          {label && (
            <label htmlFor={checkboxId} className="text-sm text-gray-700 cursor-pointer">
              {label}
            </label>
          )}
        </div>
        {error && (
          <p className="text-sm text-red-600" role="alert">
            {error}
          </p>
        )}
      </div>
    );
  }
);

Checkbox.displayName = "Checkbox";

export { Checkbox };