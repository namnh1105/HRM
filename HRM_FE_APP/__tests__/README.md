# Test Suite Documentation

## Tổng quan
Dự án Scrolla FE có một bộ test toàn diện bao gồm unit tests, integration tests và component tests cho tất cả logic quan trọng của ứng dụng.

## Cấu trúc Tests

### 1. Component Tests (`__tests__/components/`)
Tests cho các React Native components:

- **VideoCard.test.tsx**: Test cho video card component
  - Rendering và hiển thị video
  - Like, comment, share, save actions
  - Follow user functionality
  - Format numbers và duration
  - Navigation actions
  - Error handling

- **CommentsModal.test.tsx**: Test cho comments modal
  - Hiển thị danh sách comments
  - Tạo comment mới
  - Reply to comments
  - Like comments
  - Navigate to user profile
  - Loading states và empty states

### 2. Hook Tests (`__tests__/hooks/`)
Tests cho custom React hooks:

- **useAuthLogin.test.ts**: Test cho login hook
  - Input validation
  - Successful login flow
  - Error handling
  - AsyncStorage operations
  - Navigation actions

- **useVideoData.test.ts**: Test cho video data hook
  - Load videos
  - Pagination
  - Refresh functionality
  - Error handling
  - Loading states

### 3. Context Tests (`__tests__/context/`)
Tests cho React Context providers:

- **AuthContext.test.tsx**: Test cho authentication context
  - Load user from storage
  - Sign in with Google
  - Sign out functionality
  - State management
  - Error handling

### 4. Utility Tests (`__tests__/utils/`)
Tests cho utility functions:

- **constants.test.ts**: Test cho app constants
  - Color constants validation
  - Spacing constants validation
  - App metadata

- **draftVideoStorage.test.ts**: Test cho draft video storage
  - Save draft video
  - Get draft videos
  - Update draft video
  - Delete draft video
  - AsyncStorage operations

### 5. Integration Tests (`__tests__/integration/`)
Tests cho end-to-end flows:

- **flows.test.tsx**: Test cho complete user flows
  - Login flow
  - Video feed flow
  - Comment flow
  - Draft video flow
  - Like and save flow

## Chạy Tests

### Chạy tất cả tests:
```bash
npm test
```

### Chạy tests với coverage:
```bash
npm test -- --coverage
```

### Chạy một test file cụ thể:
```bash
npm test VideoCard.test.tsx
```

### Chạy tests ở watch mode:
```bash
npm test -- --watch
```

### Chạy tests với maxWorkers (tối ưu hiệu suất):
```bash
npm test -- --maxWorkers=2
```

## Test Coverage

Bộ tests bao phủ các phần chính sau:

1. **Components** (90%+)
   - VideoCard
   - CommentsModal
   - Các components UI khác

2. **Hooks** (95%+)
   - useAuthLogin
   - useVideoData
   - useAuthSignUp
   - useGoogleAuth
   - Các custom hooks khác

3. **Utilities** (100%)
   - draftVideoStorage
   - constants
   - Helper functions

4. **Context** (90%+)
   - AuthContext
   - ChatContext

5. **Integration Flows** (85%+)
   - Login flow
   - Video feed flow
   - Comment flow

## Mock Setup

File `jest.setup.js` chứa tất cả các mocks cần thiết:

- AsyncStorage
- expo-random
- expo-av
- expo-linear-gradient
- @expo/vector-icons
- React Navigation
- socket.io-client
- expo-constants

## Best Practices

1. **Isolation**: Mỗi test nên độc lập và không phụ thuộc vào test khác
2. **Mocking**: Mock tất cả external dependencies
3. **Descriptive names**: Tên test nên mô tả rõ ràng behavior được test
4. **Arrange-Act-Assert**: Tuân theo pattern AAA trong mỗi test
5. **Coverage**: Đảm bảo coverage >= 80% cho code quan trọng

## Continuous Integration

Tests được chạy tự động trong CI/CD pipeline:
- Pre-commit hooks
- Pull request checks
- Build pipeline

## Troubleshooting

### Lỗi thường gặp:

1. **Module not found**: Kiểm tra jest.config.js và moduleNameMapper
2. **Async errors**: Đảm bảo sử dụng async/await và waitFor
3. **Mock errors**: Kiểm tra jest.setup.js và mocks

### Debug tests:
```bash
npm test -- --verbose
```

## Thêm Tests Mới

Khi thêm feature mới, hãy:
1. Tạo test file tương ứng
2. Viết tests cho các cases chính
3. Viết tests cho edge cases và error cases
4. Đảm bảo coverage >= 80%
5. Run tests và đảm bảo tất cả pass

## Resources

- [Jest Documentation](https://jestjs.io/)
- [React Native Testing Library](https://callstack.github.io/react-native-testing-library/)
- [Testing Best Practices](https://kentcdodds.com/blog/common-mistakes-with-react-testing-library)
